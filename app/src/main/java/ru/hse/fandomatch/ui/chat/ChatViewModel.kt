package ru.hse.fandomatch.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.epochMillisToDateString

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadChatInfoUseCase: LoadChatInfoUseCase,
    private val subscribeToChatMessagesUseCase: SubscribeToChatMessagesUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ChatState> = MutableStateFlow(ChatState.Idle)
    val state: StateFlow<ChatState> get() = _state
    private val _action = MutableStateFlow<ChatAction?>(null)
    val action: StateFlow<ChatAction?> get() = _action

    private var _messages: StateFlow<List<Message>> = MutableStateFlow(emptyList())

    fun obtainEvent(event: ChatEvent) {
        Log.i("ChatViewModel", "Obtained event: $event")
        when (event) {
            ChatEvent.Clear -> clear()
            is ChatEvent.LoadChat -> loadChat(event.profileId)
            is ChatEvent.SendMessage -> sendMessage(event.message, event.images, event.timestamp)
            is ChatEvent.ProfileClicked -> goToProfile()
        }
    }

    private fun loadChat(profileId: String?) {
        _state.value = ChatState.Loading
        if (profileId == null) {
            _state.value = ChatState.Error
            return
        }
        viewModelScope.launch(dispatcherIO) {
            delay(1000)
            val result = loadChatInfoUseCase.execute(userId = profileId)
            if (result.isFailure) {
                Log.e("ChatViewModel", "Failed to load chat info", result.exceptionOrNull())
                _state.value = ChatState.Error
                return@launch
            }
            val chat = result.getOrNull() ?: run {
                Log.e("ChatViewModel", "Chat info is null")
                _state.value = ChatState.Error
                return@launch
            }
            // todo error handling
            _messages = subscribeToChatMessagesUseCase.execute(userId = profileId, chatId = chat.chatId)
            _state.value = ChatState.Main(
                chatId = chat.chatId,
                participantId = chat.participantId,
                participantName = chat.participantName,
                participantAvatarUrl = chat.participantAvatarUrl,
                uiElements = _messages.value.mapMessagesToUiElements().reversed(),
            )

            _messages.collect {
                Log.d("ChatViewModel", "Loaded chat messages: $it")
                _state.value = when (val currentState = _state.value) {
                    is ChatState.Main -> currentState.copy(
                        uiElements = it.mapMessagesToUiElements().reversed()
                    )
                    else -> currentState
                }
            }
        }
    }

    private fun sendMessage(message: String, images: List<ByteArray>, timestamp: Long) {
        Log.i("ChatViewModel", "Sending message: $message at $timestamp")
        when (val currentState = _state.value) {
            is ChatState.Main -> {
                viewModelScope.launch(dispatcherIO) {
                    val result = sendMessageUseCase.execute(
                        userId = currentState.participantId,
                        content = message,
                        images = images,
                        timestamp = timestamp * 1000,
                    )
                    if (result.isFailure) {
                        Log.e("ChatViewModel", "Failed to send message", result.exceptionOrNull())
                    }
                }
            }

            else -> Unit
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
        _state.value = ChatState.Idle
        _action.value = null
    }

    fun List<Message>.mapMessagesToUiElements(): List<ChatUiElement> {
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
