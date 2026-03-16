package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.repos.GlobalRepository

class LoadMessagesUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: Long): List<Message> {
        return globalRepository.loadChatMessages(
            userId = userId,
            beforeTimestamp = null,
            size = 100500, // todo
        )
    }
}
