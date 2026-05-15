package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class GetChatMessagesPageUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(
        chatId: String,
        userId: String,
        beforeTimestamp: Long?,
        size: Int,
    ): Result<List<Message>> = runCatching {
        refreshAuthUseCase.execute {
            globalRepository.getChatMessagesPage(
                chatId = chatId,
                userId = userId,
                beforeTimestamp = beforeTimestamp,
                size = size,
            )
        }
    }
}

