package ru.hse.fandomatch.domain.usecase.matches

import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class LoadSuggestedProfilesUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(size: Int): Result<List<ProfileCard>> {
        return runCatching {
            refreshAuthUseCase.execute {
                globalRepository.getSuggestedProfiles(size)
            }
        }
    }
}
