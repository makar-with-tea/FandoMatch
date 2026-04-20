package ru.hse.fandomatch.data.mock

import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.AuthInfo
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.UploadMedia
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.model.UserPreferences
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
        avatarMediaId: String?,
        password: String
    ): AuthInfo {
        mockUser = mockUser.copy(
            name = name,
            age = ((System.currentTimeMillis() - dateOfBirthMillis) / (1000L * 60 * 60 * 24 * 365)).toInt(),
            gender = gender,
            profileType = ProfileType.Own(
                email = email,
                login = login,
            ),
            avatar = avatarMediaId?.let { mockUser.avatar?.copy(id = avatarMediaId) }
        )
        return mockAuthInfo.also {
            Log.d("GlobalRepositoryMock", "register: successful for user $login")
        }
    }

    override suspend fun logout() {
        Log.d("GlobalRepositoryMock", "logout: successful for user ${(mockUser.profileType as? ProfileType.Own)?.login}")
    }

    override suspend fun refreshToken(refreshToken: String): AuthInfo {
        return if (refreshToken == mockAuthInfo.refreshToken) {
            mockAuthInfo.copy(
                accessToken = "new_mock_access_token_${System.currentTimeMillis()}",
            )
        } else {
            Log.d("GlobalRepositoryMock", "refreshToken: failed due to invalid refresh token")
            throw IllegalArgumentException("Invalid refresh token")
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
            avatar = avatarMediaId?.let { mockUser.avatar?.copy(id = avatarMediaId) },
            background = backgroundMediaId?.let { mockUser.background?.copy(id = backgroundMediaId) },
            fandoms = fandoms,
            city = city,
        )
        mockUserPosts = mockUserPosts.map {
            it.copy(
                authorName = name,
                authorAvatar = mockUser.avatar,
            )
        }
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

    override suspend fun deleteUser() {
        Log.d("GlobalRepositoryMock", "deleteUser: successful")
    }

    override suspend fun getFriends(id: String): List<OtherProfileItem> {
        return mockUsers
            .filter { it.profileType is ProfileType.Friend }
            .map {
                OtherProfileItem(
                    id = it.id,
                    name = it.name,
                    login = (it.profileType as ProfileType.Friend).login,
                    avatar = it.avatar,
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
                    avatar = it.avatar,
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

    override suspend fun getCitiesByQuery(query: String): List<City> {
        return mockCities.filter {
            it.nameRussian.contains(query, ignoreCase = true)
                    || it.nameEnglish.contains(query, ignoreCase = true)
        }.also {
            Log.d(
                "GlobalRepositoryMock",
                "getCitiesByQuery: returned ${it.size} cities for query \"$query\""
            )
        }
    }

    override suspend fun getUserPreferences(): UserPreferences {
        return mockUserPreferences.also {
            Log.d(
                "GlobalRepositoryMock",
                "getUserPreferences: returned preferences: $it"
            )
        }
    }

    override suspend fun updateUserPreferences(
        matchNotificationsEnabled: Boolean,
        messageNotificationsEnabled: Boolean,
        hideMyPostsFromNonMatches: Boolean
    ) {
        mockUserPreferences = mockUserPreferences.copy(
            matchesEnabled = matchNotificationsEnabled,
            messagesEnabled = messageNotificationsEnabled,
            hideMyPostsFromNonMatches = hideMyPostsFromNonMatches
        ).also {
            Log.d(
                "GlobalRepositoryMock",
                "updateUserPreferences: updated preferences to: $it"
            )
        }
    }

    override suspend fun changeEmail(newEmail: String) {
        val currentLogin = (mockUser.profileType as? ProfileType.Own)?.login ?: return
        mockUser = mockUser.copy(
            profileType = ProfileType.Own(
                login = currentLogin,
                email = newEmail,
            )
        )
        Log.d("GlobalRepositoryMock", "changeEmail: changed email to $newEmail")
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
        Log.d(
            "GlobalRepositoryMock",
            "subscribeToChatMessages: subscribed to chat $chatId for user $userId with size $size"
        )
        return mockMessages
    }

    override suspend fun loadChatInfo(userId: String): Chat {
        Log.d("GlobalRepositoryMock", "loadChatInfo: returned chat info for userId $userId")
        return mockChat
    }

    override suspend fun sendMessage(
        receiverId: String,
        content: String,
        mediaIdsWithTypes: List<Pair<String, MediaType>>,
        timestamp: Long
    ) {
        mockMessages.value += Message(
            messageId = (mockMessages.value.size + 1).toString(),
            isFromThisUser = true,
            content = content,
            mediaItems = mediaIdsWithTypes.map { (mediaId, type) ->
                when (type) {
                    MediaType.IMAGE -> "luffy".getMediaByName().copy(id = mediaId)
                    MediaType.VIDEO -> "noenor_edit".getMediaByName().copy(id = mediaId)
                }
            },
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

    override suspend fun getUploadMediaUrl(mediaType: MediaType): UploadMedia {
        val url = when (mediaType) {
            MediaType.IMAGE -> "https://example.com/upload/image"
            MediaType.VIDEO -> "https://example.com/upload/video"
        }
        Log.d("GlobalRepositoryMock", "getUploadMediaUrl: returned url $url for media type ${mediaType.name}")
        return UploadMedia(
            url = url,
            mediaId = "mock_media_id_${System.currentTimeMillis()}",
            expiresAt = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.UTC) * 1000,
        )
    }

    override suspend fun uploadToPresignedUrl(url: String, bytes: ByteArray, contentType: String) {
        Log.d(
            "GlobalRepositoryMock",
            "uploadToPresignedUrl: uploaded media to url $url with content type $contentType and size ${bytes.size} bytes"
        )
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

    override suspend fun createPost(
        content: String,
        mediaIdsWithTypes: List<Pair<String, MediaType>>,
        fandomIds: List<String>
    ) {
        val newPost = Post(
            id = (mockPosts.size + mockUserPosts.size + 1).toString(),
            authorId = mockUser.id,
            authorName = mockUser.name,
            authorLogin = (mockUser.profileType as ProfileType.Own).login,
            authorAvatar = mockUser.avatar,
            content = content,
            mediaItems = mediaIdsWithTypes.map { (mediaId, type) ->
                when (type) {
                    MediaType.IMAGE -> "luffy".getMediaByName().copy(id = mediaId)
                    MediaType.VIDEO -> "video".getMediaByName().copy(id = mediaId)
                }
            },
            fandoms = fandomIds.mapNotNull { id -> mockFandoms.find { it.id == id } },
            likeCount = 0,
            isLikedByCurrentUser = false,
            timestamp = System.currentTimeMillis(),
            commentCount = 0,
        )
        mockUserPosts = listOf(newPost) + mockUserPosts
        Log.d("GlobalRepositoryMock", "createPost: created new post with id ${newPost.id}")
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
