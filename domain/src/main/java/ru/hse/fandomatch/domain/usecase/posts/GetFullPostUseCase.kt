package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class GetFullPostUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(postId: String): Result<FullPost> {
        return runCatching {
            refreshAuthUseCase.execute {
                globalRepository.getFullPost(postId)
            }
        }
    }
}
