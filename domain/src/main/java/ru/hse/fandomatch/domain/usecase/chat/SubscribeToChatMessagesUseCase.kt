package ru.hse.fandomatch.domain.usecase.chat

import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.repos.GlobalRepository

class SubscribeToChatMessagesUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: String): StateFlow<List<Message>> {
        return globalRepository.subscribeToChatMessages(
            userId = userId,
            beforeTimestamp = null,
            size = 100500, // todo
        )
    }
}
