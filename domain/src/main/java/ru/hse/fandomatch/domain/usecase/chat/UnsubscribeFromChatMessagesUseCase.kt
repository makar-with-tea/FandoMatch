package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.repos.GlobalRepository

class UnsubscribeFromChatMessagesUseCase(
    private val globalRepository: GlobalRepository,
) {
    fun execute() {
        globalRepository.unsubscribeFromChatMessages()
    }
}
