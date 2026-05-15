package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.repos.GlobalRepository

class SendCommentUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(
        postId: String,
        content: String,
        timestamp: Long,
    ): Result<Unit> {
        return runCatching {
            globalRepository.sendComment(
                postId = postId,
                content = content,
                timestamp = timestamp,
            )
        }
    }
}
