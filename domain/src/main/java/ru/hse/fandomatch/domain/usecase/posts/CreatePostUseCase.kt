package ru.hse.fandomatch.domain.usecase.posts

import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.repos.GlobalRepository

class CreatePostUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(
        content: String,
        mediaIdsWithTypes: List<Pair<String, MediaType>>,
        fandomIds: List<String>,
    ): Result<Unit> = runCatching {
        globalRepository.createPost(
            content = content,
            mediaIdsWithTypes = mediaIdsWithTypes,
            fandomIds = fandomIds,
        )
    }
}
