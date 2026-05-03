package ru.hse.fandomatch.domain.usecase.chat

import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class SubscribeToChatPreviewsUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(): Result<StateFlow<List<ChatPreview>>> = runCatching {
        refreshAuthUseCase.execute {
            globalRepository.subscribeToChatPreviews(
                beforeTimestamp = null,
                size = 100500, // todo
            )
        }
    }
}
