package ru.hse.fandomatch.data.socket

import kotlinx.coroutines.flow.Flow
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.Message

interface ChatSocketService {
    fun observeChatMessages(userId: String): Flow<Message>
    fun stopObservingChatMessages()
    fun observeChatPreviews(): Flow<ChatPreview>
    fun stopObservingChatPreviews()
}
