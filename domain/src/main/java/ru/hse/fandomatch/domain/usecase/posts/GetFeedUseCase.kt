package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetFeedUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(): List<Post> {
        // todo runCatching все дела
        val userId = sharedPrefRepository.getUserId() ?: return emptyList()
        return globalRepository.getFeedPosts(
            id = userId,
            beforeTimestamp = null,
            size = 100500, // todo
        )
    }
}
