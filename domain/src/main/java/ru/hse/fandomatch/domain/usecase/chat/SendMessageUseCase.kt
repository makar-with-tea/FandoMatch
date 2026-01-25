package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.repos.GlobalRepository

class SendMessageUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: Long, content: String, timestamp: Long) {
        globalRepository.sendMessage(
            receiverId = userId,
            content = content,
            timestamp = timestamp,
        )
    }
}
