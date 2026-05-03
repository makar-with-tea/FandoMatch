package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class GetFeedUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(): Result<List<Post>> {
        return runCatching {
            val userId = sharedPrefRepository.getUserId()
                ?: throw IllegalStateException("User ID not found in shared preferences")
            refreshAuthUseCase.execute {
                globalRepository.getFeedPosts(
                    id = userId,
                    beforeTimestamp = null,
                    size = 100500, // todo
                )
            }
        }
    }
}
