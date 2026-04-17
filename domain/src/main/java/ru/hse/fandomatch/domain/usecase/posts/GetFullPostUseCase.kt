package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetFullPostUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(postId: String): Result<FullPost> {
        return runCatching { globalRepository.getFullPost(postId) }
    }
}
