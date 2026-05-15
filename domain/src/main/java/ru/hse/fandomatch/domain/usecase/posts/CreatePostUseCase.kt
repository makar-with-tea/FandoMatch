package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class CreatePostUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(
        content: String,
        mediaIdsWithTypes: List<Pair<String, MediaType>>,
        fandomIds: List<String>,
    ): Result<Unit> = runCatching {
        refreshAuthUseCase.execute {
            globalRepository.createPost(
                content = content,
                mediaIdsWithTypes = mediaIdsWithTypes,
                fandomIds = fandomIds,
            )
        }
    }
}
