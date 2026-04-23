package ru.hse.fandomatch.domain.model

data class Chat(
    val chatId: String,
    val participantId: String,
    val participantName: String,
    val participantAvatarUrl: String?,
)

data class ChatPreview(
    val chatId: String,
    val participantName: String,
    val participantAvatarUrl: String?,
    val lastMessage: String,
    val isLastMessageFromThisUser: Boolean,
    val lastMessageTimestamp: Long,
    val newMessagesCount: Int,
)

data class Message(
    val messageId: String,
    val isFromThisUser: Boolean,
    val content: String,
    val timestamp: Long,
    val mediaItems: List<MediaItem> = emptyList(),
)
