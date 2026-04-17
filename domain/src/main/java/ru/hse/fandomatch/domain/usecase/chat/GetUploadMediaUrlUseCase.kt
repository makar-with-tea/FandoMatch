package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetUploadMediaUrlUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(mediaType: MediaType): Result<String> {
        return runCatching {
            globalRepository.getUploadMediaUrl(mediaType)
        }
    }
}