package ru.hse.fandomatch.domain.usecase.media

import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.repos.MediaRepository

class DownloadMediaToGalleryUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend fun execute(
        mediaUrl: String,
        mediaType: MediaType,
    ): Result<Unit> = runCatching {
        mediaRepository.downloadMediaToGallery(
            mediaUrl = mediaUrl,
            mediaType = mediaType,
        )
    }
}

