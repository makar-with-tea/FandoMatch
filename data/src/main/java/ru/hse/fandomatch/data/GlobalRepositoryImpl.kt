package ru.hse.fandomatch.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MediaType as OkHttpMediaType
import okhttp3.RequestBody
import retrofit2.HttpException
import ru.hse.fandomatch.data.api.ChatApiService
import ru.hse.fandomatch.data.api.CoreApiService
import ru.hse.fandomatch.data.api.S3UploadApiService
import ru.hse.fandomatch.data.api.UserApiService
import ru.hse.fandomatch.data.model.ChangePasswordRequestDTO
import ru.hse.fandomatch.data.model.ChatMessagesRequestDTO
import ru.hse.fandomatch.data.model.ChatPreviewsRequestDTO
import ru.hse.fandomatch.data.model.CityDTO
import ru.hse.fandomatch.data.model.CreatePostRequestDTO
import ru.hse.fandomatch.data.model.EditUserProfileRequestDTO
import ru.hse.fandomatch.data.model.FandomCategoryDTO
import ru.hse.fandomatch.data.model.FandomDTO
import ru.hse.fandomatch.data.model.FandomRequestCreateDTO
import ru.hse.fandomatch.data.model.FriendUserProfileResponseDTO
import ru.hse.fandomatch.data.model.FullUserProfileResponseDTO
import ru.hse.fandomatch.data.model.GenderDTO
import ru.hse.fandomatch.data.model.GetFeedRequestDTO
import ru.hse.fandomatch.data.model.GetRelatedUsersRequestDTO
import ru.hse.fandomatch.data.model.MatchActionRequestDTO
import ru.hse.fandomatch.data.model.MatchActionTypeDTO
import ru.hse.fandomatch.data.model.MatchBatchRequestDTO
import ru.hse.fandomatch.data.model.MatchFilterDTO
import ru.hse.fandomatch.data.model.MatchFilterRequestDTO
import ru.hse.fandomatch.data.model.MediaTypeDTO
import ru.hse.fandomatch.data.model.PostMediaInputDTO
import ru.hse.fandomatch.data.model.PostsGetRequestDTO
import ru.hse.fandomatch.data.model.PresignedUploadRequestDTO
import ru.hse.fandomatch.data.model.PublicUserProfileResponseDTO
import ru.hse.fandomatch.data.model.ResponseStatusDTO
import ru.hse.fandomatch.data.model.SendMessageRequestDTO
import ru.hse.fandomatch.data.model.TimestampPaginationRequestDTO
import ru.hse.fandomatch.data.model.UserLoginRequestDTO
import ru.hse.fandomatch.data.model.UserProfileRequestDTO
import ru.hse.fandomatch.data.model.UserRegistrationRequestDTO
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
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.UploadMedia
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository
import kotlin.Int
import kotlin.String

class GlobalRepositoryImpl(
    private val coreApiService: CoreApiService,
    private val chatApiService: ChatApiService,
    private val userApiService: UserApiService,
    private val s3UploadApiService: S3UploadApiService,
): GlobalRepository {
    override suspend fun getUser(profileId: String): User {
        val response = coreApiService.getUserProfile(
            UserProfileRequestDTO(
                profileId
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                val user = response.successResponse!!.let { userDTO ->
                    when (userDTO) {
                        is FriendUserProfileResponseDTO -> User(
                            id = userDTO.uid,
                            fandoms = userDTO.fandoms?.map { it.toDomain() } ?: emptyList(),
                            description = userDTO.bio,
                            name = userDTO.name,
                            gender = Gender.FEMALE, // todo даша
                            age = 0, // todo даша
                            avatar = userDTO.avatar?.toDomain(),
                            background = userDTO.background?.toDomain(),
                            city = userDTO.city?.toDomain(),
                            profileType = ProfileType.Friend(login = userDTO.username)
                        )

                        is FullUserProfileResponseDTO -> User(
                            id = userDTO.uid,
                            fandoms = userDTO.fandoms.map { it.toDomain() },
                            description = userDTO.bio,
                            name = userDTO.name,
                            gender = userDTO.gender?.toDomain()
                                ?: Gender.NOT_SPECIFIED, // todo даша
                            age = userDTO.age.toInt(),
                            avatar = userDTO.avatar?.toDomain(),
                            background = userDTO.background?.toDomain(),
                            city = userDTO.city?.toDomain(),
                            profileType = ProfileType.Own(
                                login = userDTO.username,
                                email = userDTO.email ?: ""
                            )
                        )

                        is PublicUserProfileResponseDTO -> User(
                            id = userDTO.uid,
                            fandoms = userDTO.fandoms.map { it.toDomain() },
                            description = userDTO.bio,
                            name = userDTO.name,
                            gender = Gender.NOT_SPECIFIED, // todo даша
                            age = 0, // todo даша
                            avatar = userDTO.avatar?.toDomain(),
                            background = userDTO.background?.toDomain(),
                            city = userDTO.city?.toDomain(),
                            profileType = ProfileType.Stranger(userDTO.hasCurrentUserReacted)
                        )
                    }
                }
                return user
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load user profile: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun login(
        login: String,
        password: String
    ): AuthInfo {
        val response = userApiService.login(
            UserLoginRequestDTO(
                email = null,
                username = login,
                hashedPassword = password // todo hashing
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return AuthInfo(
                    accessToken = response.successResponse!!.accessToken,
                    refreshToken = response.successResponse.refreshToken,
                    userId = response.successResponse.uuid,
                )
            }

            ResponseStatusDTO.ERROR -> {
                throw Exception("Login failed: ${response.errorResponse?.errorCode}, ${response.errorResponse?.errorMessage}") // todo error handling
            }
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
        val response = userApiService.register(
            UserRegistrationRequestDTO(
                name = name,
                email = email,
                username = login,
                birthDate = dateOfBirthMillis,
                hashedPassword = password // todo hashing
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return AuthInfo(
                    accessToken = response.successResponse!!.accessToken,
                    refreshToken = response.successResponse.refreshToken,
                    userId = response.successResponse.uuid,
                )
            }

            ResponseStatusDTO.ERROR -> {
                throw Exception("Registration failed: ${response.errorResponse?.errorCode}, ${response.errorResponse?.errorMessage}") // todo error handling
            }
        }
    }

    override suspend fun updateUser(
        name: String,
        bio: String?,
        city: City?,
        fandoms: List<Fandom>,
        avatarMediaId: String?,
        backgroundMediaId: String?,
    ) {
        val response = coreApiService.editUserProfile(
            EditUserProfileRequestDTO(
                bio = bio,
                avatarMediaId = avatarMediaId,
                backgroundMediaId = backgroundMediaId,
                name = name,
                gender = GenderDTO.MALE, // todo даша убрать
                city = CityDTO.fromDomain(city ?: City("aaa", "aaa")) // todo даша nullable
            ) // todo даша добавить фандомы
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to update user profile: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        val response = userApiService.changePassword(
            ChangePasswordRequestDTO(
                oldPassword = oldPassword,
                newPassword = newPassword
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to change password: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun deleteUser(login: String) {
        // todo даша
    }

    override suspend fun getFriends(id: String): List<OtherProfileItem> {
        val response = coreApiService.getFriends(
            GetRelatedUsersRequestDTO(
                uuid = id
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.friends.map { friendDTO ->
                    OtherProfileItem(
                        id = friendDTO.uid,
                        name = friendDTO.name,
                        login = friendDTO.username,
                        avatar = friendDTO.avatar?.toDomain(),
                    )
                }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load friends: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun getFriendRequests(id: String): List<OtherProfileItem> {
        val response = coreApiService.getPendingRequests(
            GetRelatedUsersRequestDTO(
                uuid = id
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.requests.map { requestDTO ->
                    OtherProfileItem(
                        id = requestDTO.uid,
                        name = requestDTO.name,
                        login = null,
                        avatar = requestDTO.avatar?.toDomain(),
                    )
                }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load friend requests: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun getVerificationCode(email: String) {
        // todo даша
    }

    override suspend fun checkVerificationCode(code: String, email: String): Boolean {
        // todo даша
        return false
    }

    override suspend fun resetPassword(code: String, newPassword: String) {
        // todo даша
    }

    override suspend fun getCitiesByQuery(query: String): List<City> {
        return emptyList()
    }

    override suspend fun getSuggestedProfiles(
        size: Int
    ): List<ProfileCard> {
        val response = coreApiService.getMatchCandidates(
            MatchBatchRequestDTO(
                batchSize = size
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.candidates.map { candidateDTO ->
                    ProfileCard(
                        id = candidateDTO.uuid,
                        name = candidateDTO.name,
                        age = candidateDTO.age,
                        avatar = candidateDTO.avatar?.toDomain(),
                        fandoms = candidateDTO.fandoms.map { it.toDomain() },
                        gender = Gender.FEMALE, // todo даша
                        compatibilityPercentage = candidateDTO.compatibility,
                        city = candidateDTO.city?.toDomain(),
                    )
                }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load match candidates: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun likeOrDislikeProfile(
        userId: String,
        isLike: Boolean
    ) {
        val response = coreApiService.reactOnMatch(
            MatchActionRequestDTO(
                targetUuid = userId,
                action = if (isLike) MatchActionTypeDTO.LIKE else MatchActionTypeDTO.DISLIKE
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to react on match: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun getCurrentFilters(): Filters {
        val response = coreApiService.getCurrentFilters()
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                val filtersDTO = response.successResponse!!
                return Filters(
                    genders = filtersDTO.gender?.map { it.toDomain() } ?: Gender.entries,
                    minAge = filtersDTO.ageFrom ?: 16,
                    maxAge = filtersDTO.ageTo ?: 80,
                    categories = filtersDTO.fandomCategory?.map { it.toDomain() } ?: emptyList(),
                    fandoms = filtersDTO.fandomId?.map { it.toDomain() } ?: emptyList(),
                    onlyInUserCity = filtersDTO.onlyInUserCity ?: false
                )
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load current filters: $errorCode, $errorMessage")
                }
            }
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
        val request = MatchFilterRequestDTO(
            filters = MatchFilterDTO(
                gender = genders.map { GenderDTO.fromDomain(it) },
                ageFrom = minAge,
                ageTo = maxAge,
                onlyInUserCity = onlyInUserCity,
                fandomCategory = categories.map { FandomCategoryDTO.fromDomain(it) },
                fandomId = fandoms.map { FandomDTO.fromDomain(it) }
            )
        )
        val response = coreApiService.setMatchFilter(request)
        if (response.errorResponse != null) {
            throw Exception("Failed to set filters: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
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
        chatId: String,
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<Message>> {
        val response = chatApiService.getChatMessages(
            userId = userId,
            request = ChatMessagesRequestDTO(
                chatId = chatId,
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
                mediaItems = messageDTO.mediaItems?.map { it.toDomain() } ?: emptyList(),
            )
        } ?: emptyList()
        ) // todo websocket somehow
    }

    override suspend fun loadChatInfo(userId: String): Chat {
        val response = chatApiService.getChat(userId)
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.let { chatDTO ->
                    Chat(
                        chatId = chatDTO.chatId,
                        participantName = chatDTO.participantName,
                        participantAvatarUrl = chatDTO.participantAvatarUrl,
                        participantId = chatDTO.participantId
                    )
                }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load chat info: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun sendMessage(
        receiverId: String,
        content: String,
        mediaIdsWithTypes: List<Pair<String, MediaType>>,
        timestamp: Long
    ) {
        val response = chatApiService.sendMessage(
            userId = receiverId,
            request = SendMessageRequestDTO(
                content = content,
                mediaIds = mediaIdsWithTypes.map { it.first },
                timestamp = timestamp,
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to send message: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun getUploadMediaUrl(mediaType: MediaType): UploadMedia {
        val response = chatApiService.getPresignedUploadUrl(
            PresignedUploadRequestDTO(
                mediaType = MediaTypeDTO.fromDomain(mediaType)
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.toDomain()
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to get upload media url: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun uploadToPresignedUrl(
        url: String,
        bytes: ByteArray,
        contentType: String
    ) {
        val mediaType = OkHttpMediaType.parse(contentType)
        val body = RequestBody.create(mediaType, bytes)
        val response = s3UploadApiService.upload(url, body)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    override suspend fun getFeedPosts(
        id: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post> {
        val response = coreApiService.getFeed(
            GetFeedRequestDTO(
                uuid = id,
                pagination = TimestampPaginationRequestDTO(
                    cursorTimestamp = beforeTimestamp,
                    size = size
                )
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.posts.map { postDTO ->
                    Post(
                        id = postDTO.id,
                        authorId = postDTO.author.uuid,
                        authorName = postDTO.author.name!!, // todo даша
                        authorLogin = postDTO.author.username,
                        authorAvatar = postDTO.author.avatar?.toDomain(),
                        timestamp = postDTO.createdAt,
                        content = postDTO.content,
                        mediaItems = postDTO.mediaItems?.map { it.toDomain() } ?: emptyList(),
                        likeCount = postDTO.likeCount ?: 0, // todo даша
                        commentCount = postDTO.commentCount ?: 0, // todo даша
                        isLikedByCurrentUser = false, // todo даша
                        fandoms = postDTO.fandoms?.map { it.toDomain() } ?: emptyList()
                    )
                }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load feed posts: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun getUserPosts(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Post> {
        val response = coreApiService.getPosts(
            PostsGetRequestDTO(
                uuid = userId, // todo даша
                pagination = TimestampPaginationRequestDTO(
                    cursorTimestamp = beforeTimestamp,
                    size = size
                )
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.posts.map { postDTO ->
                    Post(
                        id = postDTO.id,
                        authorId = postDTO.author.uuid,
                        authorName = postDTO.author.name!!, // todo даша
                        authorLogin = postDTO.author.username,
                        authorAvatar = postDTO.author.avatar?.toDomain(),
                        timestamp = postDTO.createdAt,
                        content = postDTO.content,
                        mediaItems = postDTO.mediaItems?.map { it.toDomain() } ?: emptyList(),
                        likeCount = postDTO.likeCount ?: 0, // todo даша
                        commentCount = postDTO.commentCount ?: 0, // todo даша
                        isLikedByCurrentUser = false, // todo даша
                        fandoms = postDTO.fandoms?.map { it.toDomain() } ?: emptyList()
                    )
                }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load user posts: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun getFullPost(postId: String): FullPost {
        val response = coreApiService.getPost(postId)
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                val fullPostDTO = response.successResponse!!
                return fullPostDTO.toDomain()
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load full post: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun likePost(postId: String) {
        val response = coreApiService.likePost(postId)
        if (response.errorResponse != null) {
            throw Exception("Failed to like post: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun createPost(
        content: String,
        mediaIdsWithTypes: List<Pair<String, MediaType>>,
        fandomIds: List<String>
    ) {
        val response = coreApiService.createPost(
            CreatePostRequestDTO(
                title = "", // todo даша убрать
                content = content,
                mediaItems = mediaIdsWithTypes.map { (mediaId, mediaType) ->
                    PostMediaInputDTO(
                        mediaId = mediaId,
                        mediaType = MediaTypeDTO.fromDomain(mediaType)
                    )
                },
                fandomId = fandomIds.firstOrNull() // todo даша multiple fandoms
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to create post: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun getFandomCategories(): List<FandomCategory> {
        val response = coreApiService.getFandomCategories()
//        return response.successResponse?.categories?.map { it.toDomain() } ?: emptyList()
        return emptyList() // todo даша
    }

    override suspend fun getFandomsByQuery(query: String): List<Fandom> {
        // todo даша
        return emptyList()
    }

    override suspend fun requestNewFandom(
        userId: String,
        name: String,
        category: FandomCategory,
        description: String
    ) {
        val response = coreApiService.requestNewFandom(
            FandomRequestCreateDTO(
                authorUuid = userId,
                name = name,
                category = FandomCategoryDTO.fromDomain(category),
                description = description
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to request new fandom: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }
}
