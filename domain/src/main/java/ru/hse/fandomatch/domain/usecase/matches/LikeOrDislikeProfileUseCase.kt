package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.repos.GlobalRepository

class LikeOrDislikeProfileUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: Long, profileId: Long, isLike: Boolean) {
        globalRepository.likeOrDislikeProfile(userId, profileId, isLike)
    }
}
