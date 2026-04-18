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
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.UploadMedia
import ru.hse.fandomatch.domain.model.User

interface GlobalRepository {
    // User
    suspend fun getUser(profileId: String): User
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
        city: City?,
        fandoms: List<Fandom>,
        avatarMediaId: String?,
        backgroundMediaId: String?,
    )
    suspend fun changePassword(oldPassword: String, newPassword: String)

    suspend fun deleteUser(login: String)
    suspend fun getFriends(id: String): List<OtherProfileItem>
    suspend fun getFriendRequests(id: String): List<OtherProfileItem>
    suspend fun getVerificationCode(email: String)
    suspend fun checkVerificationCode(code: String, email: String): Boolean
    suspend fun resetPassword(code: String, newPassword: String)
    suspend fun getCitiesByQuery(query: String): List<City>

    // Matches

    suspend fun getSuggestedProfiles(size: Int): List<ProfileCard>
    suspend fun likeOrDislikeProfile(userId: String, isLike: Boolean)
    suspend fun getCurrentFilters(): Filters
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
        chatId: String,
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
    suspend fun getUploadMediaUrl(mediaType: MediaType): UploadMedia

    suspend fun uploadToPresignedUrl(
        url: String,
        bytes: ByteArray,
        contentType: String
    )

    // Posts
    suspend fun getFeedPosts(
        id: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post>
    suspend fun getUserPosts(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post>
    suspend fun getFullPost(postId: String): FullPost
    suspend fun likePost(postId: String)

    // Fandoms
    suspend fun getFandomCategories(): List<FandomCategory>
    suspend fun getFandomsByQuery(query: String): List<Fandom>
    suspend fun requestNewFandom(
        userId: String,
        name: String,
        category: FandomCategory,
        description: String,
    )
}
