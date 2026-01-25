package ru.hse.fandomatch.ui.chat

import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.Message

sealed class ChatState {
    enum class ChatError {
        IDLE,
        NETWORK,
    }
    data class Main(
        val chatId: Long,
        val participantId: Long,
        val participantName: String,
        val participantAvatarUrl: String?,
        val messages: List<Pair<Message, Boolean>>, // Boolean - needsTail in UI
        val error: ChatError = ChatError.IDLE,
    ) : ChatState()

    data object Loading : ChatState()
    data object Error : ChatState()
    data object Idle : ChatState()
}

sealed class ChatEvent {
    data class SendMessage(
        val message: String,
        val timestamp: Long,
    ) : ChatEvent()
    data class LoadChat(
        val userId: Long?,
    ) : ChatEvent()
    data object Clear : ChatEvent()
}

sealed class ChatAction {
}
