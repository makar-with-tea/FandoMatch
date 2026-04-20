package ru.hse.fandomatch.domain.repos

import ru.hse.fandomatch.domain.model.MediaType

interface MediaRepository {
    suspend fun downloadMediaToGallery(
        mediaUrl: String,
        mediaType: MediaType,
    )
}

