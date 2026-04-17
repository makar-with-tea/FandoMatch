package ru.hse.fandomatch.data.mock

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.AuthInfo
import ru.hse.fandomatch.domain.model.Comment
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository
import java.time.LocalDateTime
import java.time.ZoneOffset

class GlobalRepositoryMock: GlobalRepository {
    override suspend fun getUser(profileId: String): User {
        val result = (mockUsers + mockUser).find { it.id == profileId } ?: throw IllegalArgumentException("User with id $profileId not found")
        return result.also {
            Log.d("GlobalRepositoryMock", "getUser: $it")
        }
    }

    override suspend fun login(
        login: String,
        password: String
    ): AuthInfo {
        if (login == (mockUser.profileType as ProfileType.Own).login && password == mockPassword)
            return mockAuthInfo.also {
                Log.d("GlobalRepositoryMock", "login: successful for user $login")
            }
        else throw InvalidCredentialsException().also {
            Log.d("GlobalRepositoryMock", "login: failed for user $login")
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        login: String,
        dateOfBirthMillis: Long,
        gender: Gender,
        avatarByteArray: ByteArray?,
        password: String
    ): AuthInfo {
        mockUser = mockUser.copy(
            name = name,
            age = ((System.currentTimeMillis() - dateOfBirthMillis) / (1000L * 60 * 60 * 24 * 365)).toInt(),
            gender = gender,
            profileType = ProfileType.Own(
                email = email,
                login = login,
            )
        )
        return mockAuthInfo.also {
            Log.d("GlobalRepositoryMock", "register: successful for user $login")
        }
    }

    override suspend fun updateUser(
        name: String,
        bio: String?,
        city: City?,
        fandoms: List<Fandom>,
        avatarMediaId: String?,
        backgroundMediaId: String?
    ) {
        mockUser = mockUser.copy(
            name = name,
            description = bio,
            avatarUrl = avatarMediaId,
            backgroundUrl = backgroundMediaId,
            fandoms = fandoms,
            city = city,
        )
        Log.d("GlobalRepositoryMock", "updateUser: successful for user $name")
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        if (oldPassword == mockPassword) {
            mockPassword = newPassword
            Log.d("GlobalRepositoryMock", "changePassword: password changed successfully")
        } else {
            Log.d("GlobalRepositoryMock", "changePassword: failed to change password due to invalid old password")
        }
    }

    override suspend fun deleteUser(login: String) {
        Log.d("GlobalRepositoryMock", "deleteUser: successful for user $login")
    }

    override suspend fun getFriends(id: String): List<OtherProfileItem> {
        return mockUsers
            .filter { it.profileType is ProfileType.Friend }
            .map {
                OtherProfileItem(
                    id = it.id,
                    name = it.name,
                    login = (it.profileType as ProfileType.Friend).login,
                    avatarUrl = it.avatarUrl,
                )
            }
            .also {
                Log.d(
                    "GlobalRepositoryMock",
                    "getFriends: returned ${it.size} friends"
                )
            }
    }

    override suspend fun getFriendRequests(id: String): List<OtherProfileItem> {
        return mockUsers
            .filter { it.profileType is ProfileType.Stranger }
            .map {
                OtherProfileItem(
                    id = it.id,
                    name = it.name,
                    login = null,
                    avatarUrl = it.avatarUrl,
                )
            }
            .also {
                Log.d(
                    "GlobalRepositoryMock",
                    "getFriendRequests: returned ${it.size} friend requests"
                )
            }
    }

    override suspend fun getVerificationCode(email: String) {
        Log.d("GlobalRepositoryMock", "getVerificationCode: sent code to email $email")
    }

    override suspend fun checkVerificationCode(
        code: String,
        email: String
    ): Boolean {
        return (code == mockVerificationCode).also {
            Log.d(
                "GlobalRepositoryMock",
                "checkVerificationCode: ${if (it) "valid" else "invalid"} code $code for email $email"
            )
        }
    }

    override suspend fun resetPassword(code: String, newPassword: String) {
        if (code == mockVerificationCode) {
            mockPassword = newPassword
            Log.d("GlobalRepositoryMock", "resetPassword: password reset successful with code $code")
        } else {
            Log.d("GlobalRepositoryMock", "resetPassword: invalid code $code")
            throw IllegalArgumentException("Invalid verification code")
        }
    }

    override suspend fun getSuggestedProfiles(size: Int): List<ProfileCard> {
        return mockProfileCards.shuffled().take(size).also {
            Log.d(
                "GlobalRepositoryMock",
                "getSuggestedProfiles: returned ${it.size} profiles"
            )
        }
    }

    override suspend fun likeOrDislikeProfile(userId: String, isLike: Boolean) {
        Log.d(
            "GlobalRepositoryMock",
            "likeProfile: ${if (isLike) "" else "dis"}liked profile $userId"
        )
    }

    override suspend fun getCurrentFilters(): Filters {
        return mockFilters.also {
            Log.d("GlobalRepositoryMock", "getCurrentFilters: returned filters $it")
        }
    }

    override suspend fun setFilters(
        userId: String,
        genders: List<Gender>,
        minAge: Int?,
        maxAge: Int?,
        categories: List<FandomCategory>,
        fandoms: List<Fandom>,
        onlyInUserCity: Boolean
    ) {
        mockFilters = mockFilters.copy(
            genders = genders,
            minAge = minAge ?: mockFilters.minAge,
            maxAge = maxAge ?: mockFilters.maxAge,
            categories = categories,
            fandoms = fandoms,
            onlyInUserCity = onlyInUserCity,
        )
    }

    override suspend fun subscribeToChatPreviews(
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<ChatPreview>> {
        return mockChatPreviews.also {
            Log.d("GlobalRepositoryMock", "subscribeToChatPreviews: subscribed with size $size")
        }
    }


    override suspend fun subscribeToChatMessages(
        chatId: String,
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<Message>> {
//        val messages = if (beforeTimestamp == null) {
//            mockMessages.value
//        } else {
//            mockMessages.value.filter { it.timestamp < beforeTimestamp }
//        }
//        val result = messages
//            .sortedByDescending { it.timestamp }
//            .take(size)
//            .also {
//                Log.d(
//                    "GlobalRepositoryMock",
//                    "loadChatMessages: returned <= ${it.size} messages for userId $userId before $beforeTimestamp"
//                )
//            }
//        mockMessages.value = result todo пагинация
        return mockMessages
    }

    override suspend fun loadChatInfo(userId: String): Chat {
        Log.d("GlobalRepositoryMock", "loadChatInfo: returned chat info for userId $userId")
        return mockChat
    }

    override suspend fun sendMessage(
        receiverId: String,
        content: String,
        images: List<ByteArray>,
        timestamp: Long
    ) {
        mockMessages.value += Message(
                    messageId = (mockMessages.value.size + 1).toString(),
                    isFromThisUser = true,
                    content = content,
                    imageUrls = images.map { "luffy" }, // todo upload images and get urls
                    timestamp = timestamp,
                )

        mockChatPreviews.value = mockChatPreviews.value.map {
            if (it.participantName == mockChat.participantName) {
                it.copy(
                    lastMessage = content,
                    isLastMessageFromThisUser = true,
                    lastMessageTimestamp = timestamp,
                )
            } else it
        }
            .sortedBy {
                -it.lastMessageTimestamp
            }
    }

    override suspend fun getUploadMediaUrl(mediaType: MediaType): String {
        val url = when (mediaType) {
            MediaType.IMAGE -> "https://example.com/upload/image"
            MediaType.VIDEO -> "https://example.com/upload/video"
        }
        Log.d("GlobalRepositoryMock", "getUploadMediaUrl: returned url $url for media type ${mediaType.name}")
        return url
    }

    override suspend fun getFeedPosts(
        id: String,
        beforeTimestamp: Long?,
        size: Int,
    ): List<Post> {
        val posts = if (beforeTimestamp == null) {
            mockPosts
        } else {
            mockPosts.filter { it.timestamp < beforeTimestamp }
        }
        val result = posts
            .sortedByDescending { it.timestamp }
            .take(size)
            .also {
                Log.d(
                    "GlobalRepositoryMock",
                    "loadPosts: returned <= ${it.size} posts before $beforeTimestamp"
                )
            }
        return result
    }

    override suspend fun getUserPosts(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post> {
        val user = (mockUsers + mockUser).find { it.id == userId } ?: return emptyList()
        val type = user.profileType
        val userPosts = (mockPosts + mockUserPosts).filter { it.authorId == userId }
        var posts = if (beforeTimestamp == null) {
            userPosts
        } else {
            userPosts.filter { it.timestamp < beforeTimestamp }
        }
        if (type is ProfileType.Stranger) {
            posts = posts.map { it.copy(authorLogin = null) }
        }
        return posts
    }

    override suspend fun getFullPost(postId: String): FullPost {
        val post = (mockPosts + mockUserPosts).find { it.id == postId } ?: throw IllegalArgumentException("Post with id $postId not found")
        val comments = mockComments
        return FullPost(
            post = post,
            comments = comments,
        ).also {
            Log.d("GlobalRepositoryMock", "getFullPost: returned full post for postId $postId with ${comments.size} comments")
        }
    }

    override suspend fun likePost(postId: String) {
        mockPosts = mockPosts.map {
            if (it.id == postId) {
                if (it.isLikedByCurrentUser) {
                    it.copy(
                        likeCount = it.likeCount - 1,
                        isLikedByCurrentUser = false,
                    )
                } else {
                    it.copy(
                        likeCount = it.likeCount + 1,
                        isLikedByCurrentUser = true,
                    )
                }
            } else it
        }

        mockUserPosts = mockUserPosts.map {
            if (it.id == postId) {
                if (it.isLikedByCurrentUser) {
                    it.copy(
                        likeCount = it.likeCount - 1,
                        isLikedByCurrentUser = false,
                    )
                } else {
                    it.copy(
                        likeCount = it.likeCount + 1,
                        isLikedByCurrentUser = true,
                    )
                }
            } else it
        }
        Log.d("GlobalRepositoryMock", "likePost: liked post $postId")
    }

    override suspend fun getFandomCategories(): List<FandomCategory> {
        return FandomCategory.entries
    }

    override suspend fun getFandomsByQuery(query: String): List<Fandom> {
        return mockFandoms.filter { it.name.contains(query, ignoreCase = true) }
    }

    override suspend fun requestNewFandom(
        userId: String,
        name: String,
        category: FandomCategory,
        description: String
    ) {
        Log.i(
            "GlobalRepositoryMock",
            "requestNewFandom: user $userId requested new fandom with name $name, category ${category.name}, description $description"
        )
    }
}
