package ru.hse.fandomatch.domain.repos

import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.AuthInfo
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.User

interface GlobalRepository {
    // User
    suspend fun getUser(profileId: String): User?
    suspend fun login(login: String, password: String): AuthInfo
    suspend fun register(
        name: String,
        email: String,
        login: String,
        dateOfBirthMillis: Long,
        gender: Gender,
        avatarByteArray: ByteArray?,
        password: String
    ): AuthInfo // todo

    suspend fun updateUser(
        name: String,
        bio: String?,
        gender: Gender,
        city: City,
        avatarUrl: String?,
        backgroundUrl: String?,
    )

    suspend fun deleteUser(login: String)
    suspend fun checkPassword(login: String, password: String): Boolean
    suspend fun getFriends(): List<OtherProfileItem>
    suspend fun getFriendRequests(): List<OtherProfileItem>

    // Matches

    suspend fun getSuggestedProfiles(size: Int): List<ProfileCard>
    suspend fun likeOrDislikeProfile(userId: String, isLike: Boolean)
    suspend fun setFilters(
        userId: String,
        genders: List<Gender> = Gender.entries,
        minAge: Int? = null,
        maxAge: Int? = null,
        categories: List<FandomCategory> = listOf(),
        fandoms: List<Fandom> = listOf(),
        onlyInUserCity: Boolean = false,
    )

    // Chats
    suspend fun subscribeToChatPreviews(
        beforeTimestamp: Long?,
        size: Int,
    ): StateFlow<List<ChatPreview>>
    suspend fun subscribeToChatMessages(
        userId: String,
        beforeTimestamp: Long?,
        size: Int,
    ): StateFlow<List<Message>>
    suspend fun loadChatInfo(userId: String): Chat
    suspend fun sendMessage(
        receiverId: String,
        content: String,
        images: List<ByteArray>,
        timestamp: Long,
    )

    // Posts
    suspend fun getFeedPosts(
        beforeTimestamp: Long?,
        size: Int
    ): List<Post>
    suspend fun getUserPosts(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post>

    // Fandoms
    suspend fun getFandomCategories(): List<FandomCategory>
    suspend fun getFandomsByQuery(query: String): List<Fandom>
}
