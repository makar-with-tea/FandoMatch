package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository

class CheckVerificationCodeUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(code: String, email: String) : Result<Boolean> = runCatching {
        globalRepository.checkVerificationCode(code, email)
    }
}
