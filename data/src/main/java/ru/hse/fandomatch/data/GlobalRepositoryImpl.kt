package ru.hse.fandomatch.data

import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import ru.hse.fandomatch.data.api.CoreApiService
import ru.hse.fandomatch.data.model.EditUserProfileRequestDTO
import ru.hse.fandomatch.data.model.FandomDTO
import ru.hse.fandomatch.data.model.FriendUserProfileResponseDTO
import ru.hse.fandomatch.data.model.FullUserProfileResponseDTO
import ru.hse.fandomatch.data.model.MatchActionRequestDTO
import ru.hse.fandomatch.data.model.MatchActionTypeDTO
import ru.hse.fandomatch.data.model.MatchBatchRequestDTO
import ru.hse.fandomatch.data.model.MatchFilterRequestDTO
import ru.hse.fandomatch.data.model.PublicUserProfileResponseDTO
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
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository
import kotlin.String

class GlobalRepositoryImpl(
    private val apiService: CoreApiService
): GlobalRepository {
    override suspend fun getUserInfo(login: String): User? {
        try {
            val response = apiService.getUserProfile(
                UserProfileRequestDTO(
                    login
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
                        profileType = ProfileType.Friend(login = login)
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
                            login = login,
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
    ): Token {
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
    ): Token {
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
        apiService.editUserProfile(
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

    override suspend fun getSuggestedProfiles(
        size: Int
    ): List<ProfileCard> {
        val response = apiService.getMatchCandidates(
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
        apiService.reactOnMatch(
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
        apiService.setMatchFilter(request)
    }

    override suspend fun subscribeToChatPreviews(
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<ChatPreview>> {
        TODO("Not yet implemented")
    }

    override suspend fun subscribeToChatMessages(
        userId: String,
        beforeTimestamp: Long?,
        size: Int
    ): StateFlow<List<Message>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadChatInfo(userId: String): Chat {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(
        receiverId: String,
        content: String,
        images: List<ByteArray>,
        timestamp: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getFeedPosts(
        beforeTimestamp: Long?,
        size: Int
    ): List<Post> {
        val response = apiService.getFeed(
            page = 0,
            size = size
        )
        return response.successResponse?.posts?.map { postDTO ->
            Post(
                id = postDTO.id,
                authorId = TODO(),
                authorName = TODO(),
                authorLogin = TODO(),
                authorAvatarUrl = TODO(),
                timestamp = TODO(),
                content = postDTO.content,
                imageUrls = TODO(),
                likeCount = TODO(),
                commentCount = TODO(),
                isLikedByCurrentUser = TODO()
            )
        } ?: emptyList()
    }

    override suspend fun getFandomCategories(): List<FandomCategory> {
        val response = apiService.getFandomCategories()
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
