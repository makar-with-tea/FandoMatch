package ru.hse.fandomatch.data.model

import com.google.gson.annotations.SerializedName
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Comment
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Post

enum class ResponseStatusDTO {
    @SerializedName("SUCCESS")
    SUCCESS,

    @SerializedName("ERROR")
    ERROR
}

data class ErrorDTO(
    @SerializedName("error_code")
    val errorCode: String,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

class EmptySuccessDTO

data class TimestampPaginationRequestDTO(
    @SerializedName("cursor_timestamp")
    val cursorTimestamp: Long? = null,
    val size: Int
)

enum class MediaTypeDTO {
    @SerializedName("IMAGE")
    IMAGE,

    @SerializedName("VIDEO")
    VIDEO;

    fun toDomain() = when (this) {
        IMAGE -> MediaType.IMAGE
        VIDEO -> MediaType.VIDEO
    }
    companion object {
        fun fromDomain(mediaType: MediaType) = when (mediaType) {
            MediaType.IMAGE -> IMAGE
            MediaType.VIDEO -> VIDEO
        }
    }
}

data class MediaItemDTO(
    @SerializedName("media_id")
    val mediaId: String,
    @SerializedName("media_type")
    val mediaType: MediaTypeDTO,
    val url: String
) {
    fun toDomain() = MediaItem(
        id = mediaId,
        mediaType = when (mediaType) {
            MediaTypeDTO.IMAGE -> MediaType.IMAGE
            MediaTypeDTO.VIDEO -> MediaType.VIDEO
        },
        url = url
    )

    companion object {
        fun fromDomain(mediaItem: MediaItem) = MediaItemDTO(
            mediaId = mediaItem.id,
            mediaType = when (mediaItem.mediaType) {
                MediaType.IMAGE -> MediaTypeDTO.IMAGE
                MediaType.VIDEO -> MediaTypeDTO.VIDEO
            },
            url = mediaItem.url
        )
    }
}

enum class GenderDTO {
    @SerializedName("MALE")
    MALE,

    @SerializedName("FEMALE")
    FEMALE,

    @SerializedName("OTHER")
    OTHER;

    fun toDomain() = when (this) {
        MALE -> Gender.MALE
        FEMALE -> Gender.FEMALE
        OTHER -> Gender.NOT_SPECIFIED
    }

    companion object {
        fun fromDomain(gender: Gender) = when (gender) {
            Gender.MALE -> MALE
            Gender.FEMALE -> FEMALE
            Gender.NOT_SPECIFIED -> OTHER
        }
    }
}

enum class CityCodeDTO {
    @SerializedName("MOSCOW")
    MOSCOW,

    @SerializedName("SAINT_PETERSBURG")
    SAINT_PETERSBURG,

    @SerializedName("NOVOSIBIRSK")
    NOVOSIBIRSK,

    @SerializedName("YEKATERINBURG")
    YEKATERINBURG,

    @SerializedName("KAZAN")
    KAZAN,

    @SerializedName("NIZHNY_NOVGOROD")
    NIZHNY_NOVGOROD,

    @SerializedName("CHELYABINSK")
    CHELYABINSK,

    @SerializedName("SAMARA")
    SAMARA,

    @SerializedName("ROSTOV_ON_DON")
    ROSTOV_ON_DON,

    @SerializedName("UFA")
    UFA,

    @SerializedName("KRASNOYARSK")
    KRASNOYARSK,

    @SerializedName("VORONEZH")
    VORONEZH,

    @SerializedName("PERM")
    PERM,

    @SerializedName("VOLGOGRAD")
    VOLGOGRAD,

    @SerializedName("OTHER")
    OTHER
}

data class CityDTO(
    val code: CityCodeDTO,
    @SerializedName("name_en")
    val nameEn: String,
    @SerializedName("name_ru")
    val nameRu: String
) {
    fun toDomain() = City(
        nameRussian = nameRu,
        nameEnglish = nameEn
    )

    companion object {
        fun fromDomain(city: City) = CityDTO(
            code = when (city.nameEnglish) {
                "Moscow" -> CityCodeDTO.MOSCOW
                "Saint Petersburg" -> CityCodeDTO.SAINT_PETERSBURG
                "Novosibirsk" -> CityCodeDTO.NOVOSIBIRSK
                "Yekaterinburg" -> CityCodeDTO.YEKATERINBURG
                "Kazan" -> CityCodeDTO.KAZAN
                "Nizhny Novgorod" -> CityCodeDTO.NIZHNY_NOVGOROD
                "Chelyabinsk" -> CityCodeDTO.CHELYABINSK
                "Samara" -> CityCodeDTO.SAMARA
                "Rostov-on-Don" -> CityCodeDTO.ROSTOV_ON_DON
                "Ufa" -> CityCodeDTO.UFA
                "Krasnoyarsk" -> CityCodeDTO.KRASNOYARSK
                "Voronezh" -> CityCodeDTO.VORONEZH
                "Perm" -> CityCodeDTO.PERM
                "Volgograd" -> CityCodeDTO.VOLGOGRAD
                else -> CityCodeDTO.OTHER
            },
            nameEn = city.nameEnglish,
            nameRu = city.nameRussian
        )
    }
}

enum class FandomCategoryDTO {
    @SerializedName("ANIME_MANGA")
    ANIME_MANGA,

    @SerializedName("BOOKS")
    BOOKS,

    @SerializedName("CARTOONS")
    CARTOONS,

    @SerializedName("FILMS")
    FILMS,

    @SerializedName("TV_SERIES")
    TV_SERIES,

    @SerializedName("GAMES")
    GAMES,

    @SerializedName("TABLETOP_GAMES")
    TABLETOP_GAMES,

    @SerializedName("MUSIC")
    MUSIC,

    @SerializedName("THEATER_MUSICALS")
    THEATER_MUSICALS,

    @SerializedName("PODCASTS")
    PODCASTS,

    @SerializedName("COMICS")
    COMICS,

    @SerializedName("CELEBRITIES")
    CELEBRITIES,

    @SerializedName("SPORTS")
    SPORTS,

    @SerializedName("HISTORY")
    HISTORY,

    @SerializedName("MYTHOLOGY")
    MYTHOLOGY,

    @SerializedName("OTHER")
    OTHER;

    fun toDomain() = when (this) {
        ANIME_MANGA -> FandomCategory.ANIME_MANGA
        BOOKS -> FandomCategory.BOOKS
        CARTOONS -> FandomCategory.CARTOONS
        FILMS -> FandomCategory.FILMS
        TV_SERIES -> FandomCategory.TV_SERIES
        GAMES -> FandomCategory.GAMES
        TABLETOP_GAMES -> FandomCategory.TABLETOP_GAMES
        MUSIC -> FandomCategory.MUSIC
        THEATER_MUSICALS -> FandomCategory.THEATER_MUSICALS
        PODCASTS -> FandomCategory.PODCASTS
        COMICS -> FandomCategory.COMICS
        CELEBRITIES -> FandomCategory.CELEBRITIES
        SPORTS -> FandomCategory.SPORTS
        HISTORY -> FandomCategory.HISTORY
        MYTHOLOGY -> FandomCategory.MYTHOLOGY
        OTHER -> FandomCategory.OTHER
    }

    companion object {
        fun fromDomain(category: FandomCategory) = when (category) {
            FandomCategory.ANIME_MANGA -> ANIME_MANGA
            FandomCategory.BOOKS -> BOOKS
            FandomCategory.CARTOONS -> CARTOONS
            FandomCategory.FILMS -> FILMS
            FandomCategory.TV_SERIES -> TV_SERIES
            FandomCategory.GAMES -> GAMES
            FandomCategory.TABLETOP_GAMES -> TABLETOP_GAMES
            FandomCategory.MUSIC -> MUSIC
            FandomCategory.THEATER_MUSICALS -> THEATER_MUSICALS
            FandomCategory.PODCASTS -> PODCASTS
            FandomCategory.COMICS -> COMICS
            FandomCategory.CELEBRITIES -> CELEBRITIES
            FandomCategory.SPORTS -> SPORTS
            FandomCategory.HISTORY -> HISTORY
            FandomCategory.MYTHOLOGY -> MYTHOLOGY
            FandomCategory.OTHER -> OTHER
        }
    }
}

data class UserProfileRequestDTO(
    val username: String
)

data class UserProfileResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: BaseUserProfileDTO? = null,
    val errorResponse: ErrorDTO? = null
)

enum class ProfileTypeDTO {
    @SerializedName("OWN")
    OWN,

    @SerializedName("FRIEND")
    FRIEND,

    @SerializedName("OTHER")
    OTHER
}

sealed interface BaseUserProfileDTO {
    @get:SerializedName("profile_type")
    val profileType: ProfileTypeDTO
}

data class FullUserProfileResponseDTO(
    @SerializedName("profile_type")
    override val profileType: ProfileTypeDTO,
    val uid: String,
    val username: String,
    val email: String? = null,
    val status: String,
    @SerializedName("created_at")
    val createdAt: Long,
    val bio: String? = null,
    val avatar: MediaItemDTO? = null,
    val background: MediaItemDTO? = null,
    val name: String,
    val gender: GenderDTO? = null,
    @SerializedName("birth_date")
    val birthDate: Long,
    val age: Long,
    val city: CityDTO? = null,
    val fandoms: List<FandomDTO>
) : BaseUserProfileDTO

data class PublicUserProfileResponseDTO(
    @SerializedName("profile_type")
    override val profileType: ProfileTypeDTO,
    val uid: String,
    val name: String,
    val bio: String? = null,
    val avatar: MediaItemDTO? = null,
    val background: MediaItemDTO? = null,
    val city: CityDTO? = null,
    val fandoms: List<FandomDTO>,
    @SerializedName("has_current_user_reacted")
    val hasCurrentUserReacted: Boolean
) : BaseUserProfileDTO

data class FriendUserProfileResponseDTO(
    @SerializedName("profile_type")
    override val profileType: ProfileTypeDTO,
    val uid: String,
    val username: String,
    val name: String,
    val bio: String? = null,
    val avatar: MediaItemDTO? = null,
    val background: MediaItemDTO? = null,
    val city: CityDTO? = null,
    val fandoms: List<FandomDTO>? = null
) : BaseUserProfileDTO

data class EditUserProfileRequestDTO(
    val bio: String? = null,
    @SerializedName("avatar_media_id")
    val avatarMediaId: String? = null,
    @SerializedName("background_media_id")
    val backgroundMediaId: String? = null,
    val name: String? = null,
    val gender: GenderDTO? = null,
    val city: CityDTO? = null
)

data class EditUserProfileResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: FullUserProfileResponseDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class GetRelatedUsersRequestDTO(
    val uuid: String
)

data class FriendsDataDTO(
    val friends: List<FriendUserProfileResponseDTO>
)

data class FriendsResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: FriendsDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class PendingRequestsDataDTO(
    val requests: List<PublicUserProfileResponseDTO>
)

data class PendingRequestsResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: PendingRequestsDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class MatchBatchRequestDTO(
    @SerializedName("batch_size")
    val batchSize: Int
)

data class MatchCandidateBatchDataDTO(
    val candidates: List<MatchCandidateResponseDTO>
)

data class MatchCandidateBatchResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: MatchCandidateBatchDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class MatchCandidateResponseDTO(
    val uuid: String,
    val username: String,
    val name: String,
    val age: Int,
    val city: CityDTO? = null,
    val avatar: MediaItemDTO? = null,
    val fandoms: List<FandomDTO>,
    val compatibility: Int
)

enum class MatchActionTypeDTO {
    @SerializedName("LIKE")
    LIKE,

    @SerializedName("DISLIKE")
    DISLIKE
}

data class MatchActionRequestDTO(
    @SerializedName("target_uuid")
    val targetUuid: String,
    val action: MatchActionTypeDTO
)

enum class MatchActionResultStatusDTO {
    @SerializedName("LIKED")
    LIKED,

    @SerializedName("DISLIKED")
    DISLIKED,

    @SerializedName("MATCH")
    MATCH
}

data class MatchActionResultDTO(
    val status: MatchActionResultStatusDTO
)

data class MatchActionResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: MatchActionResultDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class MatchFilterDTO(
    val gender: List<GenderDTO>? = null,
    @SerializedName("age_from")
    val ageFrom: Int? = null,
    @SerializedName("age_to")
    val ageTo: Int? = null,
    @SerializedName("only_in_user_city")
    val onlyInUserCity: Boolean? = null,
    @SerializedName("fandom_category")
    val fandomCategory: List<FandomCategoryDTO>? = null,
    @SerializedName("fandom_id")
    val fandomId: List<FandomDTO>? = null
)

data class MatchFilterRequestDTO(
    val filters: MatchFilterDTO
)

data class MatchFilterResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)

data class CurrentFiltersResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: MatchFilterDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class PostsGetRequestDTO(
    val uuid: String,
    val pagination: TimestampPaginationRequestDTO? = null
)

data class PostMediaInputDTO(
    @SerializedName("media_id")
    val mediaId: String,
    @SerializedName("media_type")
    val mediaType: MediaTypeDTO
)

data class CreatePostRequestDTO(
    val title: String,
    val content: String,
    @SerializedName("fandom_id")
    val fandomId: String? = null,
    @SerializedName("media_items")
    val mediaItems: List<PostMediaInputDTO>? = null
)

data class PostAuthorDTO(
    val username: String,
    val uuid: String,
    val name: String? = null,
    val avatar: MediaItemDTO? = null
)

data class PostDTO(
    val id: String,
    val title: String,
    val content: String,
    @SerializedName("like_count")
    val likeCount: Int? = null,
    @SerializedName("comment_count")
    val commentCount: Int? = null,
    val author: PostAuthorDTO,
    val fandoms: List<FandomDTO>? = null,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("media_items")
    val mediaItems: List<MediaItemDTO>? = null
)

data class ExtendedPostDTO(
    val id: String,
    val title: String,
    val content: String,
    @SerializedName("like_count")
    val likeCount: Int? = null,
    @SerializedName("comment_count")
    val commentCount: Int? = null,
    val author: PostAuthorDTO,
    val fandoms: List<FandomDTO>? = null,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("media_items")
    val mediaItems: List<MediaItemDTO>? = null,
    val comments: List<CommentDTO>? = null
) {
    fun toDomain() = FullPost(
        post = Post(
            id = id,
            authorId = author.uuid,
            authorName = author.name ?: "",
            authorLogin = author.username,
            authorAvatar = author.avatar?.toDomain(),
            timestamp = createdAt,
            content = content,
            mediaItems = mediaItems?.map { it.toDomain() } ?: listOf(),
            likeCount = likeCount ?: 0,
            commentCount = commentCount ?: 0,
            isLikedByCurrentUser = false, // todo даша
            fandoms = fandoms?.map { it.toDomain() } ?: listOf()
        ),
        comments = comments?.map { it.toDomain() } ?: listOf()
    )
}

data class ExtendedPostResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: ExtendedPostDTO? = null,
    val errorResponse: ErrorDTO? = null,
)

data class CreatePostResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: PostDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class PostListDataDTO(
    val posts: List<PostDTO>
)

data class PostListResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: PostListDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class CommentsGetRequestDTO(
    @SerializedName("cursor_timestamp")
    val cursorTimestamp: Long? = null,
    val size: Int
)

data class CommentDTO(
    val id: String,
    @SerializedName("author_name")
    val authorName: String? = null,
    @SerializedName("author_username")
    val authorUsername: String,
    @SerializedName("author_avatar")
    val authorAvatar: MediaItemDTO? = null,
    val content: String,
    @SerializedName("created_at")
    val createdAt: Long
) {
    fun toDomain() = Comment(
        authorName = authorName ?: "",
        authorLogin = authorUsername,
        authorAvatar = authorAvatar?.toDomain(),
        timestamp = createdAt,
        content = content
    )
}

data class CommentListDataDTO(
    val comments: List<CommentDTO>
)

data class CommentListResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: CommentListDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

enum class PostLikeStatusDTO {
    @SerializedName("LIKED")
    LIKED,

    @SerializedName("LIKE_REMOVED")
    LIKE_REMOVED
}

data class PostLikeSuccessDTO(
    val status: PostLikeStatusDTO
)

data class PostLikeResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: PostLikeSuccessDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class GetFeedRequestDTO(
    val uuid: String,
    val pagination: TimestampPaginationRequestDTO
)

data class FandomsGetRequestDTO(
    val uuid: String
)

data class FandomListDataDTO(
    val fandoms: List<FandomDTO>
)

data class FandomListResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: FandomListDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class FandomDTO(
    val id: String,
    val name: String,
    val category: FandomCategoryDTO? = null
) {
    fun toDomain() = Fandom(
        id = id,
        name = name,
        category = category!!.toDomain() // todo даша
    )

    companion object {
        fun fromDomain(fandom: Fandom) = FandomDTO(
            id = fandom.id,
            name = fandom.name,
            category = FandomCategoryDTO.fromDomain(fandom.category),
        )
    }
}

data class FandomCategoryListDataDTO(
    val categories: List<String>
)

data class FandomCategoryListResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: FandomCategoryListDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class FandomRequestCreateDTO(
    val name: String,
    val description: String? = null,
    val category: FandomCategoryDTO,
    @SerializedName("author_uuid")
    val authorUuid: String
)

data class FandomRequestCreateResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)
