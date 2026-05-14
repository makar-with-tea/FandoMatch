package ru.hse.fandomatch.domain.usecase.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class SubscribeToChatMessagesUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(
        userId: String,
    ): Result<Flow<Message>> {
        return runCatching {
            refreshAuthUseCase.execute {
                globalRepository.subscribeToChatMessages(
                    userId = userId,
                )
            }.also {
                sharedPrefRepository.saveCurrentChatId(userId)
            }
        }
    }
}
