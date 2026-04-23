package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class DeleteAccountUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(): Result<Unit> {
        return runCatching {
            globalRepository.deleteUser()
            sharedPrefRepository.clearInfo()
        }
    }
}
