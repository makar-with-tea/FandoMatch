package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetPastLoginUseCase(
    private val sharedPrefRepository: SharedPrefRepository
) {
    suspend fun execute(): String? {
        return sharedPrefRepository.getUserId()
    }
}
