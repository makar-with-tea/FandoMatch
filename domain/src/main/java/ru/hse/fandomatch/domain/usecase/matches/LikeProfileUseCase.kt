package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.repos.GlobalRepository

class LikeProfileUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: Long, profileId: Long) {
        globalRepository.likeProfile(userId, profileId)
    }
}
