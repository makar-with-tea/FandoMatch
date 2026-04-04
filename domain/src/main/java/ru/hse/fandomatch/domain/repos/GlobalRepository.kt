package ru.hse.fandomatch.domain.repos

import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User

interface GlobalRepository {
    suspend fun getUserInfo(login: String): User?
    suspend fun login(login: String, password: String): Token
    suspend fun register(
        name: String,
        email: String,
        login: String,
        dateOfBirthMillis: Long,
        gender: Gender,
        avatarByteArray: ByteArray?,
        password: String
    ): Token // todo

    suspend fun updateUser(
        name: String? = null,
        surname: String? = null,
        email: String? = null,
        login: String,
        password: String? = null
    )

    suspend fun deleteUser(login: String)
    suspend fun checkPassword(login: String, password: String): Boolean

    suspend fun getSuggestedProfiles(userId: Long, size: Int): List<ProfileCard>
    suspend fun likeOrDislikeProfile(userId: Long, profileId: Long, isLike: Boolean)
    suspend fun subscribeToChatPreviews(
        beforeTimestamp: Long?,
        size: Int,
    ): StateFlow<List<ChatPreview>>
    suspend fun loadChatMessages(
        userId: Long,
        beforeTimestamp: Long?,
        size: Int,
    ): List<Message>
    suspend fun loadChatInfo(userId: Long): Chat
    suspend fun sendMessage(
        receiverId: Long,
        content: String,
        images: List<ByteArray>,
        timestamp: Long,
    )

    suspend fun getFeedPosts(
        beforeTimestamp: Long?,
        size: Int
    ): List<Post>
}
