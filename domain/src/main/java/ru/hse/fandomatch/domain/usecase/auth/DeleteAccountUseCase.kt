package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class DeleteAccountUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(): Result<Unit> {
        return runCatching {
            refreshAuthUseCase.execute {
                globalRepository.deleteUser()
                sharedPrefRepository.clearInfo()
            }
        }
    }
}
