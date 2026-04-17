package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.repos.GlobalRepository

class LikePostUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(postId: String) : Result<Unit> {
        return runCatching {
            globalRepository.likePost(postId)
        }
    }
}