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
import ru.hse.fandomatch.data.mock.mockChat
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.domain.model.Message
import java.sql.Timestamp

class ChatViewModel(
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
            is ChatEvent.LoadChat -> loadChat(event.chatId)
            is ChatEvent.SendMessage -> sendMessage(event.message, event.timestamp)
        }
    }

    private fun loadChat(chatId: Long?) {
        _state.value = ChatState.Loading
        // todo
        viewModelScope.launch(dispatcherIO) {
            delay(1000)
            _state.value = ChatState.Main(
                chatId = mockChat.chatId,
                participantId = mockChat.participantId,
                participantName = mockChat.participantName,
                participantAvatarUrl = mockChat.participantAvatarUrl,
                messages = mockChat.messages.mapIndexed { index, message ->
                    val needsTail = if (index == mockChat.messages.size - 1) {
                        true
                    } else {
                        mockChat.messages[index + 1].isFromThisUser != message.isFromThisUser
                    }
                    Pair(message, needsTail)
                }.reversed(),
            )
        }
    }

    private fun sendMessage(message: String, timestamp: Long) {
        // todo
        Log.i("ChatViewModel", "Sending message: $message at $timestamp")
        _state.value = when (val currentState = _state.value) {
            is ChatState.Main -> {
                val newMessage = Message(
                    messageId = currentState.messages.size.toLong() + 1, // todo normal id
                    isFromThisUser = true,
                    content = message,
                    timestamp = timestamp, // todo что там с миллисекундами?
                )
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

    private fun clear() {
        _state.value = ChatState.Idle
        _action.value = null
    }
}
