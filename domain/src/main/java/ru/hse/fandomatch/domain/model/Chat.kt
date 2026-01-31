package ru.hse.fandomatch.domain.model

data class Chat(
    val chatId: Long,
    val participantId: Long,
    val participantName: String,
    val participantAvatarUrl: String?,
    val messages: List<Message>,
)

data class ChatPreview(
    val chatId: Long,
    val participantName: String,
    val participantAvatarUrl: String?,
    val lastMessage: String,
    val isLastMessageFromThisUser: Boolean,
    val lastMessageTimestamp: Long,
    val newMessagesCount: Int,
)

data class Message(
    val messageId: Long,
    val isFromThisUser: Boolean,
    val content: String,
    val timestamp: Long,
    val imageUrls: List<String> = emptyList(),
)
