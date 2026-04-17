package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetUserPostsUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun execute(profileId: String?, isCurrentUser: Boolean): List<Post> {
        val id = when {
            !profileId.isNullOrEmpty() -> profileId
            isCurrentUser -> sharedPrefRepository.getUserId()
            else -> null
        } ?: return emptyList()
        return globalRepository.getUserPosts(id, null, 100500) // todo pagination
    }
}