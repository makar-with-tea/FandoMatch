package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository

class ChangeEmailUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(newEmail: String): Result<Unit> = runCatching {
        globalRepository.changeEmail(newEmail)
    }
}
