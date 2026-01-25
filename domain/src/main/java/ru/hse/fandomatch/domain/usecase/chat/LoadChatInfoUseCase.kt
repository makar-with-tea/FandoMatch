package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.repos.GlobalRepository

class LoadChatInfoUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: Long): Chat {
        return globalRepository.loadChatInfo(
            userId = userId,
        )
    }
}
