package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.repos.GlobalRepository

class CheckVerificationCodeUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(code: String) : Boolean {
        return globalRepository.checkVerificationCode(code)
    }
}