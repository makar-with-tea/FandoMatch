package ru.hse.fandomatch.data.model

import com.google.gson.annotations.SerializedName

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

data class EmptySuccessDTO(
    val placeholder: String? = null
)

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
    val profileType: ProfileTypeDTO
}

data class FullUserProfileResponseDTO(
    @SerializedName("profile_type")
    override val profileType: ProfileTypeDTO,
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    val bio: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    @SerializedName("background_url")
    val backgroundUrl: String? = null,
    val name: String? = null,
    val gender: String? = null,
    @SerializedName("birth_date")
    val birthDate: String? = null,
    val city: String? = null,
    val fandoms: List<FandomDTO>? = null
): BaseUserProfileDTO

data class PublicUserProfileResponseDTO(
    @SerializedName("profile_type")
    override val profileType: ProfileTypeDTO,
    val username: String,
    val name: String,
    val bio: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    @SerializedName("background_url")
    val backgroundUrl: String? = null,
    val city: String? = null,
    val fandoms: List<FandomDTO>? = null
): BaseUserProfileDTO

data class FriendUserProfileResponseDTO(
    @SerializedName("profile_type")
    override val profileType: ProfileTypeDTO,
    val name: String,
    val bio: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    @SerializedName("background_url")
    val backgroundUrl: String? = null,
    val city: String? = null,
    val fandoms: List<FandomDTO>? = null
): BaseUserProfileDTO

data class EditUserProfileRequestDTO(
    val bio: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    @SerializedName("background_url")
    val backgroundUrl: String? = null,
    val name: String? = null,
    val gender: String? = null,
    @SerializedName("birth_date")
    val birthDate: String? = null,
    val city: String? = null
)

data class EditUserProfileResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: FullUserProfileResponseDTO? = null,
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
    val uuid: String? = null,
    val username: String,
    val name: String? = null,
    val age: Int? = null,
    val city: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    val fandoms: List<String>? = null,
    val compatibility: Int? = null
)

enum class MatchActionTypeDTO {
    @SerializedName("LIKE")
    LIKE,

    @SerializedName("DISLIKE")
    DISLIKE
}

data class MatchActionRequestDTO(
    @SerializedName("target_uuid") // todo даша
    val targetUuid: String, // todo даша
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
    val status: MatchActionResultStatusDTO,
    @SerializedName("match_chat_id")
    val matchChatId: String? = null
)

data class MatchActionResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: MatchActionResultDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class MatchFilterRequestDTO(
    val gender: String? = null,
    @SerializedName("age_from")
    val ageFrom: Int? = null,
    @SerializedName("age_to")
    val ageTo: Int? = null,
    val city: String? = null,
    @SerializedName("fandom_category")
    val fandomCategory: String? = null,
    @SerializedName("fandom_id")
    val fandomId: String? = null
)

data class MatchFilterSuccessDTO(
    val placeholder: String? = null
)

data class MatchFilterResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: MatchFilterSuccessDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class PostsGetRequestDTO(
    val username: String,
    val page: Int? = null,
    val size: Int? = null
)

data class CreatePostRequestDTO(
    val title: String,
    val content: String,
    @SerializedName("fandom_id")
    val fandomId: String? = null
)

data class PostDTO(
    val id: String,
    val title: String,
    val content: String,
    @SerializedName("created_at")
    val createdAt: String
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

data class CommentDTO(
    val id: String? = null,
    val author: String? = null,
    val content: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

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
    LIKED
}

data class PostLikeSuccessDTO(
    val status: PostLikeStatusDTO? = null
)

data class PostLikeResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: PostLikeSuccessDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class FandomsGetRequestDTO(
    val username: String
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
    val description: String? = null
)

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
    val category: String? = null,
    @SerializedName("author_username")
    val authorUsername: String
)

enum class FandomRequestCreateStatusDTO {
    @SerializedName("RECEIVED")
    RECEIVED
}

data class FandomRequestCreateSuccessDTO(
    val status: FandomRequestCreateStatusDTO? = null
)

data class FandomRequestCreateResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: FandomRequestCreateSuccessDTO? = null,
    val errorResponse: ErrorDTO? = null
)
