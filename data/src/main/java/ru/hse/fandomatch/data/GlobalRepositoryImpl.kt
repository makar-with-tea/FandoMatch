package ru.hse.fandomatch.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.RequestBody
import retrofit2.HttpException
import ru.hse.fandomatch.data.api.ChatApiService
import ru.hse.fandomatch.data.api.CoreApiService
import ru.hse.fandomatch.data.api.S3UploadApiService
import ru.hse.fandomatch.data.api.UserApiService
import ru.hse.fandomatch.data.model.ChangeEmailRequestDTO
import ru.hse.fandomatch.data.model.ChangePasswordRequestDTO
import ru.hse.fandomatch.data.model.ChatMessagesRequestDTO
import ru.hse.fandomatch.data.model.ChatPreviewsRequestDTO
import ru.hse.fandomatch.data.model.CheckVerificationCodeRequestDTO
import ru.hse.fandomatch.data.model.CityDTO
import ru.hse.fandomatch.data.model.CreateCommentRequestDTO
import ru.hse.fandomatch.data.model.CreatePostRequestDTO
import ru.hse.fandomatch.data.model.DeviceTokenRequestDTO
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
import ru.hse.fandomatch.data.model.RefreshTokenDTO
import ru.hse.fandomatch.data.model.ResetPasswordRequestDTO
import ru.hse.fandomatch.data.model.ResponseStatusDTO
import ru.hse.fandomatch.data.model.SendMessageRequestDTO
import ru.hse.fandomatch.data.model.SendVerificationCodeRequestDTO
import ru.hse.fandomatch.data.model.TimestampPaginationRequestDTO
import ru.hse.fandomatch.data.model.UpdateUserPreferencesRequestDTO
import ru.hse.fandomatch.data.model.UserLoginRequestDTO
import ru.hse.fandomatch.data.model.UserProfileRequestDTO
import ru.hse.fandomatch.data.model.UserRegistrationRequestDTO
import ru.hse.fandomatch.data.socket.ChatSocketService
import ru.hse.fandomatch.domain.exception.EmailAlreadyInUseException
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.exception.LoginAlreadyInUseException
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
import okhttp3.MediaType as OkHttpMediaType

class GlobalRepositoryImpl(
    private val coreApiService: CoreApiService,
    private val chatApiService: ChatApiService,
    private val userApiService: UserApiService,
    private val s3UploadApiService: S3UploadApiService,
    private val chatSocketService: ChatSocketService,
): GlobalRepository {
    private val repositoryScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val chatMessagesJobs = mutableMapOf<String, Job>()
    private var chatPreviewsJob: Job? = null

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
                            gender = userDTO.gender.toDomain(),
                            age = userDTO.age.toInt(),
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
                            gender = userDTO.gender.toDomain(),
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
                            gender = userDTO.gender.toDomain(),
                            age = userDTO.age.toInt(),
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
                response.errorResponse!!.let {
                    it.checkAuth()
                    throw Exception("Failed to load user profile: ${it.errorCode}, ${it.errorMessage}")
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
                username = login,
                hashedPassword = password
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
                if (response.errorResponse?.errorCode == "CREDENTIALS_MISMATCH") {
                    throw InvalidCredentialsException()
                }
                throw Exception("Login failed: ${response.errorResponse?.errorCode}, ${response.errorResponse?.errorMessage}")
            }
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        login: String,
        dateOfBirthEpochSeconds: Long,
        gender: Gender,
        password: String
    ): AuthInfo {
        val response = userApiService.register(
            UserRegistrationRequestDTO(
                name = name,
                email = email,
                username = login,
                birthDate = dateOfBirthEpochSeconds,
                hashedPassword = password,
                gender = GenderDTO.fromDomain(gender).name,
                avatarMediaId = null,
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
                if (response.errorResponse?.errorCode == "USERNAME_ALREADY_EXISTS") {
                    throw LoginAlreadyInUseException()
                }
                if (response.errorResponse?.errorCode == "EMAIL_ALREADY_EXISTS") {
                    throw EmailAlreadyInUseException()
                }
                throw Exception("Registration failed: ${response.errorResponse?.errorCode}, ${response.errorResponse?.errorMessage}")
            }
        }
    }

    override suspend fun logout() {
        val response = userApiService.logout()
        if (response.errorResponse != null) {
            throw Exception("Logout failed: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun refreshToken(
        refreshToken: String
    ): AuthInfo {
        val response = userApiService.refreshToken(
            RefreshTokenDTO(
                refreshToken = refreshToken
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
                throw Exception("Token refresh failed: ${response.errorResponse?.errorCode}, ${response.errorResponse?.errorMessage}")
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
                city = city?.let { CityDTO.fromDomain(it) },
                fandomIds = fandoms.map { FandomDTO.fromDomain(it).id },
            )
        )
        response.errorResponse?.let {
            it.checkAuth()
            throw Exception("Failed to update user profile: ${it.errorCode}, ${it.errorMessage}")
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        val response = userApiService.changePassword(
            ChangePasswordRequestDTO(
                oldPassword = oldPassword,
                newPassword = newPassword,
            )
        )
        if (response.errorResponse != null) {
            if (response.errorResponse.errorCode == "CREDENTIALS_MISMATCH") {
                throw InvalidCredentialsException()
            }
            throw Exception("Failed to change password: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun deleteUser() {
        val response = userApiService.deleteProfile()
        response.errorResponse?.let {
            it.checkAuth()
            throw Exception("Failed to delete user profile: ${it.errorCode}, ${it.errorMessage}")
        }
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
        val response = userApiService.sendVerificationCode(
            SendVerificationCodeRequestDTO(
                email = email
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to send verification code: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun checkVerificationCode(code: String, email: String): Boolean {
        val response = userApiService.checkVerificationCode(
            CheckVerificationCodeRequestDTO(
                code = code,
                email = email
            )
        )
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.result ?: false
            }

            ResponseStatusDTO.ERROR -> {
                if (response.errorResponse?.errorCode == "VERIFICATION_CODE_INVALID") {
                    return false
                }
                throw Exception("Failed to check verification code: ${response.errorResponse?.errorCode}, ${response.errorResponse?.errorMessage}")
            }
        }
    }

    override suspend fun resetPassword(code: String, newPassword: String, email: String) {
        val response = userApiService.resetPassword(
            ResetPasswordRequestDTO(
                code = code,
                email = email,
                newPassword = newPassword
            )
        )
        if (response.errorResponse != null) {
            if (response.errorResponse.errorCode == "VERIFICATION_CODE_INVALID") {
                throw IllegalArgumentException("Invalid verification code")
            }
                throw Exception("Failed to reset password: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun getCitiesByQuery(query: String): List<City> {
        val response = coreApiService.searchCities(query)
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.cities.map { it.toDomain() }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to load cities: $errorCode, $errorMessage")
                }
            }
        }
    }

    override suspend fun getUserPreferences(): UserPreferences {
        val response = coreApiService.getUserPreferences()
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                val preferencesDTO = response.successResponse!!
                return UserPreferences(
                    matchesEnabled = preferencesDTO.matchNotificationsEnabled,
                    messagesEnabled = preferencesDTO.messageNotificationsEnabled,
                    hideMyPostsFromNonMatches = preferencesDTO.hideMyPostsFromNonMatches
                )
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let {
                    it.checkAuth()
                    throw Exception("Failed to load user preferences: ${it.errorCode}, ${it.errorMessage}")
                }
            }
        }
    }

    override suspend fun updateUserPreferences(
        matchNotificationsEnabled: Boolean,
        messageNotificationsEnabled: Boolean,
        hideMyPostsFromNonMatches: Boolean
    ) {
        val response = coreApiService.updateUserPreferences(
            UpdateUserPreferencesRequestDTO(
                matchNotificationsEnabled = matchNotificationsEnabled,
                messageNotificationsEnabled = messageNotificationsEnabled,
                hideMyPostsFromNonMatches = hideMyPostsFromNonMatches
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to update user preferences: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun changeEmail(newEmail: String) {
        val response = userApiService.changeEmail(
            ChangeEmailRequestDTO(
                newEmail = newEmail
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to change email: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun saveDeviceToken(token: String, userId: String?) {
        userApiService.saveDeviceToken(
            DeviceTokenRequestDTO(
                fcmToken = token,
                userId = userId,
            )
        )
    }

    override suspend fun getSuggestedProfiles(
        size: Int
    ): List<ProfileCard> {
        val response = coreApiService.getMatchCandidates(
            MatchBatchRequestDTO(
                batchSize = size
            )
        )
        response.errorResponse?.let {
            it.checkAuth()
            throw Exception("Failed to load suggested profiles: ${it.errorCode}, ${it.errorMessage}")
        }
        return response.successResponse?.let { successResponse ->
            successResponse.candidates.map { candidateDTO ->
                ProfileCard(
                    id = candidateDTO.uuid,
                    name = candidateDTO.name,
                    age = candidateDTO.age,
                    avatar = candidateDTO.avatar?.toDomain(),
                    fandoms = candidateDTO.fandoms.map { it.toDomain() },
                    gender = candidateDTO.gender.toDomain(),
                    compatibilityPercentage = candidateDTO.compatibility,
                    city = candidateDTO.city?.toDomain(),
                )
            }
        } ?: emptyList()
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
                response.errorResponse!!.let {
                    it.checkAuth()
                    throw Exception("Failed to load current filters: ${it.errorCode}, ${it.errorMessage}")
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
            ChatPreviewsRequestDTO(beforeTimestamp = beforeTimestamp, size = size)
        )
        response.errorResponse?.let {
            it.checkAuth()
            throw Exception("Failed to load chat previews: ${it.errorCode}, ${it.errorMessage}")
        }
        val initial = response.successResponse?.previews?.map { dto ->
            ChatPreview(
                chatId = dto.chatId,
                participantName = dto.participantName,
                participantAvatarUrl = dto.participantAvatarUrl,
                lastMessage = dto.lastMessage,
                isLastMessageFromThisUser = dto.isLastMessageFromThisUser,
                lastMessageTimestamp = dto.lastMessageTimestamp,
                newMessagesCount = dto.newMessagesCount,
            )
        } ?: emptyList()

        val state = MutableStateFlow(initial)

        chatPreviewsJob?.cancel()
        chatPreviewsJob = chatSocketService.observeChatPreviews()
            .onEach { incoming ->
                val updated = state.value
                    .filterNot { it.chatId == incoming.chatId } + incoming
                state.value = updated.sortedByDescending { it.lastMessageTimestamp }
            }
            .launchIn(repositoryScope)

        return state
    }

    override suspend fun subscribeToChatMessages(
        chatId: String,
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<Message>> {
        val initial = getChatMessagesPage(
            chatId = chatId,
            userId = userId,
            beforeTimestamp = beforeTimestamp,
            size = size
        )
        val state = MutableStateFlow(initial)

        chatMessagesJobs[userId]?.cancel()
        chatMessagesJobs[userId] = chatSocketService.observeChatMessages(userId)
            .onEach { incoming ->
                state.value = (state.value + incoming)
                    .distinctBy { it.messageId }
                    .sortedBy { it.timestamp }
            }
            .launchIn(repositoryScope)

        return state
    }

    override suspend fun getChatMessagesPage(
        chatId: String,
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): List<Message> {
        val response = chatApiService.getChatMessages(
            userId = userId,
            request = ChatMessagesRequestDTO(
                chatId = chatId,
                beforeTimestamp = beforeTimestamp,
                size = size
            )
        )
        response.errorResponse?.let {
            it.checkAuth()
            throw Exception("Failed to load chat messages: ${it.errorCode}, ${it.errorMessage}")
        }
        return response.successResponse?.messages?.map { dto ->
            Message(
                messageId = dto.messageId,
                isFromThisUser = dto.isFromThisUser,
                content = dto.content,
                timestamp = dto.timestamp,
                mediaItems = dto.mediaItems?.map { it.toDomain() } ?: emptyList(),
            )
        } ?: emptyList()
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
                        authorName = postDTO.author.name.orEmpty(),
                        authorLogin = postDTO.author.username,
                        authorAvatar = postDTO.author.avatar?.toDomain(),
                        timestamp = postDTO.createdAt,
                        content = postDTO.content,
                        mediaItems = postDTO.mediaItems?.map { it.toDomain() } ?: emptyList(),
                        likeCount = postDTO.likeCount,
                        commentCount = postDTO.commentCount,
                        isLikedByCurrentUser = postDTO.isLikedByCurrentUser,
                        fandoms = postDTO.fandoms?.map { it.toDomain() } ?: emptyList()
                    )
                }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let {
                    it.checkAuth()
                    throw Exception("Failed to load feed posts: ${it.errorCode}, ${it.errorMessage}")
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
                uuid = userId,
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
                        authorName = postDTO.author.name!!,
                        authorLogin = postDTO.author.username,
                        authorAvatar = postDTO.author.avatar?.toDomain(),
                        timestamp = postDTO.createdAt,
                        content = postDTO.content,
                        mediaItems = postDTO.mediaItems?.map { it.toDomain() } ?: emptyList(),
                        likeCount = postDTO.likeCount,
                        commentCount = postDTO.commentCount,
                        isLikedByCurrentUser = postDTO.isLikedByCurrentUser,
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
                response.errorResponse!!.let {
                    it.checkAuth()
                    throw Exception("Failed to load full post: ${it.errorCode}, ${it.errorMessage}")
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
                content = content,
                mediaItems = mediaIdsWithTypes.map { (mediaId, mediaType) ->
                    PostMediaInputDTO(
                        mediaId = mediaId,
                        mediaType = MediaTypeDTO.fromDomain(mediaType)
                    )
                },
                fandomIds = fandomIds
            )
        )
        response.errorResponse?.let {
            it.checkAuth()
            throw Exception("Failed to create post: ${it.errorCode}, ${it.errorMessage}")
        }
    }

    override suspend fun sendComment(postId: String, content: String, timestamp: Long) {
        val response = coreApiService.createComment(
            postId = postId,
            request = CreateCommentRequestDTO(
                content = content,
                timestamp = timestamp
            )
        )
        if (response.errorResponse != null) {
            throw Exception("Failed to send comment: ${response.errorResponse.errorCode}, ${response.errorResponse.errorMessage}")
        }
    }

    override suspend fun getFandomCategories(): List<FandomCategory> {
        val response = coreApiService.getFandomCategories()
        return response.successResponse?.categories?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getFandomsByQuery(query: String): List<Fandom> {
        val response = coreApiService.searchFandoms(query)
        when (response.status) {
            ResponseStatusDTO.SUCCESS -> {
                return response.successResponse!!.fandoms.map { it.toDomain() }
            }

            ResponseStatusDTO.ERROR -> {
                response.errorResponse!!.let { (errorCode, errorMessage) ->
                    throw Exception("Failed to search fandoms: $errorCode, $errorMessage")
                }
            }
        }
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
        response.errorResponse?.let {
            it.checkAuth()
            throw Exception("Failed to request new fandom: ${it.errorCode}, ${it.errorMessage}")
        }
    }
}
