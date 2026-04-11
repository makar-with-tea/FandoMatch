package ru.hse.fandomatch.domain.model

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorLogin: String?,
    val authorAvatarUrl: String?,
    val timestamp: Long,
    val content: String?,
    val imageUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean,
)
