package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class LogoutUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute() {
        sharedPrefRepository.clearInfo()
    }
}
