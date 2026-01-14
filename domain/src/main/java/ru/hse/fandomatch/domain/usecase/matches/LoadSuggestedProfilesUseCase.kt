package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.repos.GlobalRepository

class LoadSuggestedProfilesUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(userId: Long, size: Int): List<ProfileCard> {
        return globalRepository.getSuggestedProfiles(userId, size)
    }
}
