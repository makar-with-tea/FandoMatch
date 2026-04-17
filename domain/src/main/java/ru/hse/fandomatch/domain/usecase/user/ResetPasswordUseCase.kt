package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.repos.GlobalRepository

class ResetPasswordUseCase(
    private val globalRepository: GlobalRepository
) {
    suspend fun execute(
        code: String,
        newPassword: String
    ) {
        globalRepository.resetPassword(code, newPassword)
    }
}