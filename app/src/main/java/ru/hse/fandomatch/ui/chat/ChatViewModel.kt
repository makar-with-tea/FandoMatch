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
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.usecase.chat.GetChatMessagesPageUseCase
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.media.DownloadMediaToGalleryUseCase
import ru.hse.fandomatch.utils.epochSecondsToDateString

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadChatInfoUseCase: LoadChatInfoUseCase,
    private val subscribeToChatMessagesUseCase: SubscribeToChatMessagesUseCase,
    private val getChatMessagesPageUseCase: GetChatMessagesPageUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val downloadMediaToGalleryUseCase: DownloadMediaToGalleryUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ChatState> = MutableStateFlow(ChatState.Idle)
    val state: StateFlow<ChatState> get() = _state
    private val _action = MutableStateFlow<ChatAction?>(null)
    val action: StateFlow<ChatAction?> get() = _action

    private var liveMessages: List<Message> = emptyList()
    private var olderMessages: List<Message> = emptyList()
    private var hasMoreOlderMessages: Boolean = true
    private var isLoadingMoreMessages: Boolean = false
    private var activeChat: Chat? = null

    private val stateMutex = Mutex()
    private var loadChatJob: Job? = null

    private companion object {
        const val MESSAGES_BLOCK_SIZE = 30
    }

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
            is ChatEvent.DownloadMediaItem -> downloadMediaItem(event.mediaItem)
            ChatEvent.LoadOlderMessages -> loadOlderMessages()
            ChatEvent.ToastShown -> toastShown()
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

            liveMessages = emptyList()
            olderMessages = emptyList()
            hasMoreOlderMessages = true
            isLoadingMoreMessages = false
            activeChat = null

            loadChatInfoUseCase.execute(userId = profileId)
                .onFailure { exception ->
                    Log.e("ChatViewModel", "Failed to load chat info", exception)
                    setState { ChatState.Error }
                    return@launch
                }
                .onSuccess { chat ->
                    subscribeToChatMessagesUseCase.execute(
                        userId = profileId,
                        chatId = chat.chatId,
                        size = MESSAGES_BLOCK_SIZE,
                    )
                        .onFailure { exception ->
                            Log.e(
                                "ChatViewModel",
                                "Failed to subscribe to chat messages",
                                exception
                            )
                            setState { ChatState.Error }
                            return@launch
                        }
                        .onSuccess { messagesFlow ->
                            activeChat = chat
                            liveMessages = messagesFlow.value.sortedBy { it.timestamp }
                            hasMoreOlderMessages =
                                messagesFlow.value.size >= MESSAGES_BLOCK_SIZE
                            renderMainState(chat)

                            messagesFlow.collect { messages ->
                                liveMessages = messages.sortedBy { it.timestamp }
                                renderMainState(chat)
                            }
                        }
                }
        }
    }

    private fun loadOlderMessages() {
        val current = _state.value as? ChatState.Main ?: return
        if (isLoadingMoreMessages || !hasMoreOlderMessages) return

        val timestamp = mergedMessages().minOfOrNull { it.timestamp } ?: return
        isLoadingMoreMessages = true
        _state.value = current.copy(isLoadingMore = true)

        viewModelScope.launch(dispatcherIO) {
            getChatMessagesPageUseCase.execute(
                chatId = current.chatId,
                userId = current.participantId,
                beforeTimestamp = timestamp,
                size = MESSAGES_BLOCK_SIZE,
            )
                .onFailure { exception ->
                    Log.e("ChatViewModel", "Failed to load older messages", exception)
                    isLoadingMoreMessages = false
                    withContext(dispatcherMain) {
                        val state = _state.value as? ChatState.Main ?: return@withContext
                        _state.value = state.copy(isLoadingMore = false)
                    }
                }
                .onSuccess { older ->
                    olderMessages = (olderMessages + older)
                        .distinctBy { it.messageId }
                        .sortedBy { it.timestamp }
                    hasMoreOlderMessages = older.size == MESSAGES_BLOCK_SIZE
                    isLoadingMoreMessages = false
                    activeChat?.let { renderMainState(it) }
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
            sendMessageUseCase.execute(
                userId = currentState.participantId,
                content = message,
                mediaIdsWithTypes = mediaIds,
                timestamp = timestamp,
            )
                .onFailure { exception ->
                Log.e("ChatViewModel", "Failed to send message", exception)
            }
                .onSuccess {
            setState { current ->
                (current as? ChatState.Main)?.copy(
                    messageDraft = "",
                    attachedFilesWithTypes = emptyList()
                ) ?: current
            }
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

    private fun downloadMediaItem(mediaItem: MediaItem) {
        viewModelScope.launch(dispatcherIO) {
            downloadMediaToGalleryUseCase.execute(
                mediaUrl = mediaItem.url,
                mediaType = mediaItem.mediaType
            )
                .onFailure {
                    Log.e("ChatViewModel", "Failed to download media item", it)
                    _action.value = ChatAction.ShowErrorDownloadToast
                }
                .onSuccess {
                    _action.value = ChatAction.ShowSuccessDownloadToast
                }
        }
    }

    private fun toastShown() {
        _action.value = null
    }

    private fun clear() {
        loadChatJob?.cancel()
        liveMessages = emptyList()
        olderMessages = emptyList()
        hasMoreOlderMessages = true
        isLoadingMoreMessages = false
        activeChat = null
        viewModelScope.launch(dispatcherMain) {
            setState { ChatState.Idle }
            _action.value = null
        }
    }

    private suspend fun renderMainState(chat: Chat) {
        val messages = mergedMessages()
        setState { current ->
            val currentMain = current as? ChatState.Main
            ChatState.Main(
                chatId = chat.chatId,
                participantId = chat.participantId,
                participantName = chat.participantName,
                participantAvatarUrl = chat.participantAvatarUrl,
                uiElements = messages.mapMessagesToUiElements().reversed(),
                hasMoreOlder = hasMoreOlderMessages,
                isLoadingMore = isLoadingMoreMessages,
                attachedFilesWithTypes = currentMain?.attachedFilesWithTypes ?: emptyList(),
                messageDraft = currentMain?.messageDraft ?: "",
                error = currentMain?.error ?: ChatState.ChatError.IDLE,
            )
        }
    }

    private fun mergedMessages(): List<Message> =
        (olderMessages + liveMessages)
            .distinctBy { it.messageId }
            .sortedBy { it.timestamp }

    private fun List<Message>.mapMessagesToUiElements(): List<ChatUiElement> {
        if (isEmpty()) return emptyList()

        val result = mutableListOf<ChatUiElement>()
        var lastDate: String? = null
        for ((index, message) in withIndex()) {
            val dateString = message.timestamp.epochSecondsToDateString()
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
