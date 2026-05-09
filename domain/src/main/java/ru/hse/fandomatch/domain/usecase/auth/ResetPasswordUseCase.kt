package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository

class ResetPasswordUseCase(
    private val globalRepository: GlobalRepository
) {
    suspend fun execute(
        code: String,
        newPassword: String,
        email: String,
    ): Result<Unit> = runCatching {
        globalRepository.resetPassword(code, newPassword, email)
    }
}