package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.repos.GlobalRepository

class UnsubscribeFromChatPreviewsUseCase(
    private val globalRepository: GlobalRepository,
) {
    fun execute() {
        globalRepository.unsubscribeFromChatPreviews()
    }
}
