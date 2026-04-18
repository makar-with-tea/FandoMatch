package ru.hse.fandomatch.domain.usecase.chat

import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.UploadMedia
import ru.hse.fandomatch.domain.repos.GlobalRepository

class UploadMediaUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(bytes: ByteArray, mediaType: MediaType): Result<String> {
        return runCatching {
            val uploadMedia = globalRepository.getUploadMediaUrl(mediaType)

            val contentType = when (mediaType) {
                MediaType.IMAGE -> "image/jpeg"
                MediaType.VIDEO -> "video/mp4"
            }

            globalRepository.uploadToPresignedUrl(
                url = uploadMedia.url,
                bytes = bytes,
                contentType = contentType
            )

            uploadMedia.mediaId
        }
    }
}
