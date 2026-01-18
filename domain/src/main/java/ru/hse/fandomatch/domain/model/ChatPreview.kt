package ru.hse.fandomatch.domain.model

data class ChatPreview(
    val chatId: Long,
    val participantName: String,
    val participantAvatarUrl: String?,
    val lastMessage: String,
    val isLastMessageFromThisUser: Boolean,
    val lastMessageTimestamp: Long,
    val newMessagesCount: Int,
)
