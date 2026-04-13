package ru.hse.fandomatch.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import ru.hse.fandomatch.data.api.ChatApiService
import ru.hse.fandomatch.data.api.CoreApiService
import ru.hse.fandomatch.data.model.ChatMessagesRequestDTO
import ru.hse.fandomatch.data.model.ChatPreviewsRequestDTO
import ru.hse.fandomatch.data.model.EditUserProfileRequestDTO
import ru.hse.fandomatch.data.model.FandomDTO
import ru.hse.fandomatch.data.model.FriendUserProfileResponseDTO
import ru.hse.fandomatch.data.model.FullUserProfileResponseDTO
import ru.hse.fandomatch.data.model.MatchActionRequestDTO
import ru.hse.fandomatch.data.model.MatchActionTypeDTO
import ru.hse.fandomatch.data.model.MatchBatchRequestDTO
import ru.hse.fandomatch.data.model.MatchFilterRequestDTO
import ru.hse.fandomatch.data.model.PostsGetRequestDTO
import ru.hse.fandomatch.data.model.PublicUserProfileResponseDTO
import ru.hse.fandomatch.data.model.SendMessageRequestDTO
import ru.hse.fandomatch.data.model.UserProfileRequestDTO
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
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository
import kotlin.String

class GlobalRepositoryImpl(
    private val coreApiService: CoreApiService,
    private val chatApiService: ChatApiService,
): GlobalRepository {
    override suspend fun getUser(profileId: String): User? {
        try {
            val response = coreApiService.getUserProfile(
                UserProfileRequestDTO(
                    profileId
                )
            )
            val user = response.successResponse?.let { userDTO ->
                when (userDTO) {
                    is FriendUserProfileResponseDTO -> User(
                        id = "", // todo даша
                        fandoms = userDTO.fandoms?.map { it.toDomain() } ?: emptyList(),
                        description = userDTO.bio,
                        name = userDTO.name,
                        gender = Gender.FEMALE, // todo даша
                        age = 0, // todo даша
                        avatarUrl = userDTO.avatarUrl,
                        backgroundUrl = userDTO.backgroundUrl,
                        city = userDTO.city?.let {
                            City(
                                nameRussian = it,
                                nameEnglish = it
                            ) // todo даша
                        },
                        profileType = ProfileType.Friend(login = "") // todo даша
                    )

                    is FullUserProfileResponseDTO -> User(
                        id = "", // todo даша
                        fandoms = userDTO.fandoms?.map { it.toDomain() } ?: emptyList(),
                        description = userDTO.bio,
                        name = userDTO.name ?: "", // todo даша
                        gender = userDTO.gender?.genderToDomain()
                            ?: Gender.NOT_SPECIFIED, // todo даша
                        age = 0, // todo даша
                        avatarUrl = userDTO.avatarUrl,
                        backgroundUrl = userDTO.backgroundUrl,
                        city = userDTO.city?.let {
                            City(
                                nameRussian = it,
                                nameEnglish = it
                            ) // todo даша
                        },
                        profileType = ProfileType.Own(
                            login = "", // todo даша
                            email = userDTO.email ?: ""
                        ) // todo даша
                    )

                    is PublicUserProfileResponseDTO -> User(
                        id = "", // todo даша
                        fandoms = userDTO.fandoms?.map { it.toDomain() } ?: emptyList(),
                        description = userDTO.bio,
                        name = userDTO.name, // todo даша
                        gender = Gender.NOT_SPECIFIED, // todo даша
                        age = 0, // todo даша
                        avatarUrl = userDTO.avatarUrl,
                        backgroundUrl = userDTO.backgroundUrl,
                        city = userDTO.city?.let {
                            City(
                                nameRussian = it,
                                nameEnglish = it
                            ) // todo даша
                        },
                        profileType = ProfileType.Stranger
                    )
                }
            }
            return user
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 404) {
                return null // User not found
            }
            throw e // Re-throw other exceptions
        }
    }

    override suspend fun login(
        login: String,
        password: String
    ): AuthInfo {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(
        name: String,
        bio: String?,
        gender: Gender,
        city: City,
        avatarUrl: String?,
        backgroundUrl: String?,
    ) {
        coreApiService.editUserProfile(
            EditUserProfileRequestDTO(
                bio = bio,
                avatarUrl = avatarUrl,
                backgroundUrl = backgroundUrl,
                name = name,
                gender = when (gender) {
                    Gender.FEMALE -> "FEMALE"
                    Gender.MALE -> "MALE"
                    Gender.NOT_SPECIFIED -> "NOT_SPECIFIED"
                },
                birthDate = null,
                city = null //todo даша
            )
        )
    }

    override suspend fun deleteUser(login: String) {
        TODO("Not yet implemented")
    }

    override suspend fun checkPassword(
        login: String,
        password: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getFriends(): List<OtherProfileItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getFriendRequests(): List<OtherProfileItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getVerificationCode(email: String) {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(code: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getSuggestedProfiles(
        size: Int
    ): List<ProfileCard> {
        val response = coreApiService.getMatchCandidates(
            MatchBatchRequestDTO(
                batchSize = size
            )
        )
        return if (response.successResponse != null) {
            response.successResponse.candidates.map { candidateDTO ->
                ProfileCard(
                    id = candidateDTO.uuid!!,
                    name = candidateDTO.name!!,
                    age = candidateDTO.age!!,
                    avatarUrl = candidateDTO.avatarUrl,
                    fandoms = emptyList(), // todo даша
                    gender = Gender.FEMALE, // todo даша
                    compatibilityPercentage = candidateDTO.compatibility!!,
                    city = candidateDTO.city?.let {
                        City(
                            nameRussian = it,
                            nameEnglish = it
                        ) // todo даша
                    }
                )
            }
        } else {
            emptyList()
        }
    }

    override suspend fun likeOrDislikeProfile(
        userId: String,
        isLike: Boolean
    ) {
        coreApiService.reactOnMatch(
            MatchActionRequestDTO(
                targetUuid = userId,
                action = if (isLike) MatchActionTypeDTO.LIKE else MatchActionTypeDTO.DISLIKE
            )
        )
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
        val request = MatchFilterRequestDTO(
            gender = genders.first().name, // todo даша
            ageFrom = minAge,
            ageTo = maxAge,
            fandomCategory = categories.map { it.name }.first(), // todo даша
            fandomId = fandoms.map { it.id }.firstOrNull(), // todo даша
        )
        coreApiService.setMatchFilter(request)
    }

    override suspend fun subscribeToChatPreviews(
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<ChatPreview>> {
        val response = chatApiService.getChatPreviews(
            ChatPreviewsRequestDTO(
                beforeTimestamp = beforeTimestamp,
                size = size
            )
        )
        return MutableStateFlow(response.successResponse?.previews?.map { previewDTO ->
            ChatPreview(
                chatId = previewDTO.chatId,
                participantName = previewDTO.participantName,
                participantAvatarUrl = previewDTO.participantAvatarUrl,
                lastMessage = previewDTO.lastMessage,
                isLastMessageFromThisUser = previewDTO.isLastMessageFromThisUser,
                lastMessageTimestamp = previewDTO.lastMessageTimestamp,
                newMessagesCount = previewDTO.newMessagesCount,
            )
        } ?: emptyList()
        ) // todo websocket somehow
    }

    override suspend fun subscribeToChatMessages(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<Message>> {
        val response = chatApiService.getChatMessages(
            userId = userId,
            request = ChatMessagesRequestDTO(
                beforeTimestamp = beforeTimestamp,
                size = size
            )
        )
        return MutableStateFlow(response.successResponse?.messages?.map { messageDTO ->
            Message(
                messageId = messageDTO.messageId,
                isFromThisUser = messageDTO.isFromThisUser,
                content = messageDTO.content,
                timestamp = messageDTO.timestamp,
                imageUrls = messageDTO.mediaItems?.map { it.url } ?: emptyList(),
            )
        } ?: emptyList()
        ) // todo websocket somehow
    }

    override suspend fun loadChatInfo(userId: String): Chat {
        val response = chatApiService.getChat(userId)
        return response.successResponse?.let { chatDTO ->
            Chat(
                chatId = chatDTO.chatId,
                participantName = chatDTO.participantName,
                participantAvatarUrl = chatDTO.participantAvatarUrl,
                participantId = chatDTO.participantId
            )
        } ?: throw Exception("Failed to load chat info") // todo error handling
    }

    override suspend fun sendMessage(
        receiverId: String,
        content: String,
        images: List<ByteArray>,
        timestamp: Long
    ) {
        // todo upload images and get mediaIds
        chatApiService.sendMessage(
            userId = receiverId,
            request = SendMessageRequestDTO(
                content = content,
                mediaIds = emptyList(),
                timestamp = timestamp,
            )
        )
    }

    override suspend fun getFeedPosts(
        beforeTimestamp: Long?,
        size: Int
    ): List<Post> {
        val response = coreApiService.getFeed(
            page = 0,
            size = size
        )
        return response.successResponse?.posts?.map { postDTO ->
            Post(
                id = postDTO.id,
                authorId = "", // todo даша
                authorName = "", // todo даша
                authorLogin = "", // todo даша
                authorAvatarUrl = "", // todo даша
                timestamp = 0L, // todo даша
                content = postDTO.content,
                imageUrls = emptyList(), // todo даша
                likeCount = 0, // todo даша
                commentCount = 0, // todo даша
                isLikedByCurrentUser = false // todo даша
            )
        } ?: emptyList()
    }

    override suspend fun getUserPosts(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post> {
        val response = coreApiService.getPosts(
            PostsGetRequestDTO(
                username = userId, // todo даша
                page = 0, // todo даша
                size = size
            )
        )
        return response.successResponse?.posts?.map { postDTO ->
            Post(
                id = postDTO.id,
                authorId = "", // todo даша
                authorName = "", // todo даша
                authorLogin = "", // todo даша
                authorAvatarUrl = "", // todo даша
                timestamp = 0L, // todo даша
                content = postDTO.content,
                imageUrls = emptyList(), // todo даша
                likeCount = 0, // todo даша
                commentCount = 0, // todo даша
                isLikedByCurrentUser = false // todo даша
            )
        } ?: emptyList()
    }

    override suspend fun getFandomCategories(): List<FandomCategory> {
        val response = coreApiService.getFandomCategories()
        return response.successResponse?.categories?.map { categoryDTO ->
            FandomCategory.BOOKS // todo даша
        } ?: emptyList()
    }

    override suspend fun getFandomsByQuery(query: String): List<Fandom> {
        TODO("Даша")
    }
}

private fun FandomDTO.toDomain(): Fandom {
    return Fandom(
        id = id,
        name = name,
        category = FandomCategory.CARTOONS // todo даша
    )
}

private fun String.genderToDomain(): Gender {
    return when (this) {
        "FEMALE" -> Gender.FEMALE
        "MALE" -> Gender.MALE
        else -> Gender.NOT_SPECIFIED
    }
}
