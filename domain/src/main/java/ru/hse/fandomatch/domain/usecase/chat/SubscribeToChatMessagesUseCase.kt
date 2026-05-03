package ru.hse.fandomatch.domain.usecase.chat

import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class SubscribeToChatMessagesUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(userId: String, chatId: String): Result<StateFlow<List<Message>>> {
        return runCatching {
            refreshAuthUseCase.execute {
                globalRepository.subscribeToChatMessages(
                    userId = userId,
                    chatId = chatId,
                    beforeTimestamp = null,
                    size = 100500, // todo
                )
            }
        }
    }
}
