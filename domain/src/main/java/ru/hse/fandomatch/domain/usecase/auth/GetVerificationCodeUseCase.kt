package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetVerificationCodeUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(email: String): Result<Unit> = runCatching {
        globalRepository.getVerificationCode(email)
    }
}
