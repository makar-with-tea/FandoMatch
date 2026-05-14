package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class UnsubscribeFromChatMessagesUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
    private val globalRepository: GlobalRepository,
) {
    fun execute() {
        globalRepository.unsubscribeFromChatMessages()
        sharedPrefRepository.saveCurrentChatId(null)
    }
}
