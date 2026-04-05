package ru.hse.fandomatch.ui.chat

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
        val uiElements: List<ChatUiElement>,
        val error: ChatError = ChatError.IDLE,
    ) : ChatState()

    data object Loading : ChatState()
    data object Error : ChatState()
    data object Idle : ChatState()
}

sealed class ChatEvent {
    data class SendMessage(
        val message: String,
        val images: List<ByteArray>,
        val timestamp: Long,
    ) : ChatEvent()
    data class LoadChat(
        val userId: Long?,
    ) : ChatEvent()
    data object Clear : ChatEvent()
}

sealed class ChatAction {
}

sealed class ChatUiElement {
    data class MessageElement(
        val message: Message,
        val hasTail: Boolean,
    ) : ChatUiElement()

    data class DayElement(
        val dateString: String,
    ) : ChatUiElement()
}
