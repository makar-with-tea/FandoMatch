package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetFriendsUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(): Result<List<OtherProfileItem>> {
        return runCatching {
            val userId = sharedPrefRepository.getUserId() ?: throw RuntimeException("User ID not found in shared preferences")
            globalRepository.getFriends(userId)
        }
    }
}
