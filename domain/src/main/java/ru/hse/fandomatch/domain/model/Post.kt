package ru.hse.fandomatch.domain.model

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorLogin: String?,
    val authorAvatarUrl: String?,
    val timestamp: Long,
    val content: String?,
    val mediaItems: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean,
    val fandoms: List<Fandom>,
)

enum class MediaType {
    IMAGE, VIDEO
}

data class MediaItem(
    val mediaId: String,
    val mediaType: MediaType,
    val mediaUrl: String
)

data class Comment(
    val authorName: String,
    val authorLogin: String?,
    val authorAvatarUrl: String?,
    val timestamp: Long,
    val content: String,
)

data class FullPost(
    val post: Post,
    val comments: List<Comment>,
)
