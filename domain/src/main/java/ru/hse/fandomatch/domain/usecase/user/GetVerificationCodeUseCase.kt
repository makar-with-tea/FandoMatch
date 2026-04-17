package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetVerificationCodeUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(email: String) {
        globalRepository.getVerificationCode(email)
    }
}
