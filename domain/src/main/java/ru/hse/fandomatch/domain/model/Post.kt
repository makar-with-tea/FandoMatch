package ru.hse.fandomatch.domain.model

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorLogin: String?,
    val authorAvatar: MediaItem?,
    val timestamp: Long,
    val content: String?,
    val mediaItems: List<MediaItem>,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean,
    val fandoms: List<Fandom>,
)

enum class MediaType {
    IMAGE, VIDEO
}

data class MediaItem(
    val id: String,
    val mediaType: MediaType,
    val url: String
)

data class UploadMedia(
    val url: String,
    val mediaId: String,
    val expiresAt: Long,
)

data class Comment(
    val authorName: String,
    val authorLogin: String?,
    val authorAvatar: MediaItem?,
    val timestamp: Long,
    val content: String,
)

data class FullPost(
    val post: Post,
    val comments: List<Comment>,
)
