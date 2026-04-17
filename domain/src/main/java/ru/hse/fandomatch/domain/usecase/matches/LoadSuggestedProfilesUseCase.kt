package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.repos.GlobalRepository

class LoadSuggestedProfilesUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(size: Int): Result<List<ProfileCard>> {
        return runCatching {
            globalRepository.getSuggestedProfiles(size)
        }
    }
}
