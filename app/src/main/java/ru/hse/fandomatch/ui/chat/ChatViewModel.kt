package ru.hse.fandomatch.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.utils.epochMillisToDateString

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadChatInfoUseCase: LoadChatInfoUseCase,
    private val subscribeToChatMessagesUseCase: SubscribeToChatMessagesUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ChatState> = MutableStateFlow(ChatState.Idle)
    val state: StateFlow<ChatState> get() = _state
    private val _action = MutableStateFlow<ChatAction?>(null)
    val action: StateFlow<ChatAction?> get() = _action

    private var _messages: StateFlow<List<Message>> = MutableStateFlow(emptyList())

    private val stateMutex = Mutex()
    private var loadChatJob: Job? = null

    private suspend fun setState(reducer: (ChatState) -> ChatState) {
        withContext(dispatcherMain) {
            stateMutex.withLock {
                _state.value = reducer(_state.value)
            }
        }
    }

    fun obtainEvent(event: ChatEvent) {
        Log.i("ChatViewModel", "Obtained event: $event")
        when (event) {
            ChatEvent.Clear -> clear()
            is ChatEvent.LoadChat -> loadChat(event.profileId)
            is ChatEvent.SendMessage -> sendMessage(event.timestamp)
            is ChatEvent.MessageDraftChanged -> draftChanged(event.draft)
            is ChatEvent.AttachmentsChanged -> attachmentsChanged(event.filesWithTypes)
            is ChatEvent.ProfileClicked -> goToProfile()
        }
    }

    private fun loadChat(profileId: String?) {
        loadChatJob?.cancel()
        loadChatJob = viewModelScope.launch(dispatcherIO) {
            setState { ChatState.Loading }

            if (profileId == null) {
                setState { ChatState.Error }
                return@launch
            }

            val result = loadChatInfoUseCase.execute(userId = profileId)
            val chat = result.getOrNull() ?: run {
                Log.e("ChatViewModel", "Failed to load chat info", result.exceptionOrNull())
                setState { ChatState.Error }
                return@launch
            }

            val messagesResult = subscribeToChatMessagesUseCase.execute(
                userId = profileId,
                chatId = chat.chatId
            )
            val messagesFlow = messagesResult.getOrNull() ?: run {
                Log.e("ChatViewModel", "Failed to subscribe to chat messages", result.exceptionOrNull())
                setState { ChatState.Error }
                return@launch
            }
            _messages = messagesFlow

            setState {
                ChatState.Main(
                    chatId = chat.chatId,
                    participantId = chat.participantId,
                    participantName = chat.participantName,
                    participantAvatarUrl = chat.participantAvatarUrl,
                    uiElements = messagesFlow.value.mapMessagesToUiElements().reversed(),
                )
            }

            messagesFlow.collect { messages ->
                setState { current ->
                    if (current is ChatState.Main) {
                        current.copy(
                            uiElements = messages.mapMessagesToUiElements().reversed()
                        )
                    } else current
                }
            }
        }
    }

    private fun draftChanged(draft: String) {
        viewModelScope.launch(dispatcherMain) {
            setState { current ->
                (current as? ChatState.Main)?.copy(messageDraft = draft) ?: current
            }
        }
    }

    private fun attachmentsChanged(filesWithTypes: List<Pair<ByteArray, MediaType>>) {
        viewModelScope.launch(dispatcherMain) {
            setState { current ->
                (current as? ChatState.Main)?.copy(attachedFilesWithTypes = filesWithTypes) ?: current
            }
        }
    }

    private fun sendMessage(timestamp: Long) {
        val currentState = _state.value as? ChatState.Main ?: return
        val message = currentState.messageDraft
        val filesWithTypes = currentState.attachedFilesWithTypes
        if (message.isBlank() && filesWithTypes.isEmpty()) return
        Log.i("ChatViewModel", "Sending message: $message at $timestamp")
        viewModelScope.launch(dispatcherIO) {
            val mediaIds = filesWithTypes.mapNotNull { (bytes, type) ->
                val uploadInfoResult = uploadMediaUseCase.execute(bytes, type)
                val mediaId = uploadInfoResult.getOrNull()
                mediaId ?: run {
                    Log.e(
                        "ChatViewModel",
                        "Failed to get upload media url",
                        uploadInfoResult.exceptionOrNull()
                    )
                    return@mapNotNull null
                }
                mediaId to type
            }
            val result = sendMessageUseCase.execute(
                userId = currentState.participantId,
                content = message,
                mediaIdsWithTypes = mediaIds,
                timestamp = timestamp * 1000,
            )
            if (result.isFailure) {
                Log.e("ChatViewModel", "Failed to send message", result.exceptionOrNull())
                return@launch
            }
            setState { current ->
                (current as? ChatState.Main)?.copy(
                    messageDraft = "",
                    attachedFilesWithTypes = emptyList()
                ) ?: current
            }
        }
    }

    private fun goToProfile() {
        when (val currentState = _state.value) {
            is ChatState.Main -> _action.value = ChatAction.GoToProfile(
                profileId = currentState.participantId
            )
            else -> Unit
        }
    }

    private fun clear() {
        loadChatJob?.cancel()
        viewModelScope.launch(dispatcherMain) {
            setState { ChatState.Idle }
            _action.value = null
        }
    }

    private fun List<Message>.mapMessagesToUiElements(): List<ChatUiElement> {
        if (isEmpty()) return emptyList()

        val result = mutableListOf<ChatUiElement>()
        var lastDate: String? = null
        for ((index, message) in withIndex()) {
            val dateString = message.timestamp.epochMillisToDateString()
            if (dateString != lastDate) {
                result.add(ChatUiElement.DayElement(dateString))
                lastDate = dateString
            }
            val hasTail = index == size - 1 || this[index + 1].isFromThisUser != message.isFromThisUser
            result.add(
                ChatUiElement.MessageElement(
                    message = message,
                    hasTail = hasTail
                )
            )
        }
        return result
    }
}
