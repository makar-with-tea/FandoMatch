package ru.hse.fandomatch.domain.usecase.chat

import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.repos.GlobalRepository

class SubscribeToChatPreviewsUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(): StateFlow<List<ChatPreview>> {
        return globalRepository.subscribeToChatPreviews(
            beforeTimestamp = null,
            size = 100500, // todo
        )
    }
}
