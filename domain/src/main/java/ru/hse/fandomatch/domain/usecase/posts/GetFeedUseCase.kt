package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetFeedUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(): Result<List<Post>> {
        return runCatching {
            val userId = sharedPrefRepository.getUserId()
                ?: throw IllegalStateException("User ID not found in shared preferences")
            globalRepository.getFeedPosts(
                id = userId,
                beforeTimestamp = null,
                size = 100500, // todo
            )
        }
    }
}
