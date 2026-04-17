package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetUserPostsUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(profileId: String?, isCurrentUser: Boolean): Result<List<Post>> {
        return runCatching {
            val id = when {
                !profileId.isNullOrEmpty() -> profileId
                isCurrentUser -> sharedPrefRepository.getUserId()
                else -> null
            } ?: throw RuntimeException("No user ID provided for fetching posts")
            globalRepository.getUserPosts(id, null, 100500) // todo pagination
        }
    }
}
