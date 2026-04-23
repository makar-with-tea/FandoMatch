package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository

class ChangePasswordUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(oldPassword: String, newPassword: String): Result<Unit> = runCatching {
        globalRepository.changePassword(oldPassword, newPassword)
    }
}
