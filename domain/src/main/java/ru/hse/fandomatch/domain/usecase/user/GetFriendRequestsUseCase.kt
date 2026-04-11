package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetFriendRequestsUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(): List<OtherProfileItem> {
        return globalRepository.getFriendRequests()
    }
}
