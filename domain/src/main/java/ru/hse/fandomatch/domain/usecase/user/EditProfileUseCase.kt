package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class EditProfileUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(
        name: String,
        bio: String?,
        city: City?,
        fandoms: List<Fandom>,
        avatarMediaId: String?,
        backgroundMediaId: String?,
    ): Result<Unit> {
        return runCatching {
            refreshAuthUseCase.execute {
                globalRepository.updateUser(
                    name = name,
                    bio = bio,
                    city = city,
                    fandoms = fandoms,
                    avatarMediaId = avatarMediaId,
                    backgroundMediaId = backgroundMediaId,
                )
            }
        }
    }
}
