package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.repos.GlobalRepository

class DislikeProfileUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: Long, profileId: Long) {
        globalRepository.dislikeProfile(userId, profileId)
    }
}
