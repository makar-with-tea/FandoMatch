package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetCurrentChatIdUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
) {
    fun execute(): String? {
        return sharedPrefRepository.getCurrentChatId()
    }
}
