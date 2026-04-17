package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.repos.GlobalRepository

class EditProfileUseCase(
    private val globalRepository: GlobalRepository,
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
