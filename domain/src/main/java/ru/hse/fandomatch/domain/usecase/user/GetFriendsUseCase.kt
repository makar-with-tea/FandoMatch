package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetFriendsUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(): List<OtherProfileItem> {
        // todo runCatching все дела
        val userId = sharedPrefRepository.getUserId() ?: return emptyList()
        return globalRepository.getFriends(userId)
    }
}
