package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetUserIdUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
) {
    fun execute(): String? {
        return sharedPrefRepository.getUserId()
    }
}