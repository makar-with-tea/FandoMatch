package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetUserUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(profileId: String?, isCurrentUser: Boolean): Result<User> {
        val id = when {
            !profileId.isNullOrEmpty() -> profileId
            isCurrentUser -> sharedPrefRepository.getUserId()
            else -> null
        } ?: return Result.failure(IllegalArgumentException("Profile ID must be provided for non-current user"))
        return runCatching {
            globalRepository.getUser(id)
        }
    }
}
