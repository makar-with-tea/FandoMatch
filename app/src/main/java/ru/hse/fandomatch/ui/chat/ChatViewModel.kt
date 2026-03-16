package ru.hse.fandomatch.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadChatInfoUseCase: LoadChatInfoUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ChatState> = MutableStateFlow(ChatState.Idle)
    val state: StateFlow<ChatState> get() = _state
    private val _action = MutableStateFlow<ChatAction?>(null)
    val action: StateFlow<ChatAction?> get() = _action

    fun obtainEvent(event: ChatEvent) {
        Log.i("ChatViewModel", "Obtained event: $event")
        when (event) {
            ChatEvent.Clear -> clear()
            is ChatEvent.LoadChat -> loadChat(event.userId)
            is ChatEvent.SendMessage -> sendMessage(event.message, event.images, event.timestamp)
        }
    }

    private fun loadChat(userId: Long?) {
        _state.value = ChatState.Loading
        // todo
        if (userId == null) {
            _state.value = ChatState.Error
            return
        }
        viewModelScope.launch(dispatcherIO) {
            delay(1000)
            val chat = loadChatInfoUseCase.execute(userId = userId)
            _state.value = ChatState.Main(
                chatId = chat.chatId,
                participantId = chat.participantId,
                participantName = chat.participantName,
                participantAvatarUrl = chat.participantAvatarUrl,
                messages = chat.messages.mapIndexed { index, message ->
                    val needsTail = if (index == chat.messages.size - 1) {
                        true
                    } else {
                        chat.messages[index + 1].isFromThisUser != message.isFromThisUser
                    }
                    Pair(message, needsTail)
                }.reversed(),
            )
        }
    }

    private fun sendMessage(message: String, images: List<ByteArray>, timestamp: Long) {
        // todo
        Log.i("ChatViewModel", "Sending message: $message at $timestamp")
        _state.value = when (val currentState = _state.value) {
            is ChatState.Main -> {
                val newMessage = Message(
                    messageId = currentState.messages.size.toLong() + 1, // todo normal id
                    isFromThisUser = true,
                    content = message,
                    imageUrls = images.map { "luffy"}, // todo upload images and get urls
                    timestamp = timestamp * 1000, // todo что там с миллисекундами?
                )
                viewModelScope.launch(dispatcherIO) {
                    sendMessageUseCase.execute(
                        userId = currentState.participantId,
                        content = message,
                        images = images,
                        timestamp = timestamp * 1000,
                    )
                }
                val updatedMessages = listOf(newMessage to true) + if (currentState.messages.isNotEmpty()) {
                    // update previous message to not have tail
                    val (lastMessage, _) = currentState.messages[0]
                    listOf(lastMessage to false) + currentState.messages.drop(1)
                } else {
                    currentState.messages
                }
                currentState.copy(messages = updatedMessages)
            }

            else -> currentState
        }
    }

    // todo polling for new messages

    private fun clear() {
        _state.value = ChatState.Idle
        _action.value = null
    }
}
