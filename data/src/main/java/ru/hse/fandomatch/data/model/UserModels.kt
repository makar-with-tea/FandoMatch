package ru.hse.fandomatch.data.model

import com.google.gson.annotations.SerializedName

data class UserRegistrationRequestDTO(
    val email: String? = null,
    val phone: String? = null,
    val username: String,
    @SerializedName("hashed_password")
    val hashedPassword: String
)

data class UserRegistrationResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: RefreshAndAccessTokensDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class UserLoginRequestDTO(
    val email: String? = null,
    val phone: String? = null,
    val username: String? = null,
    @SerializedName("hashed_password")
    val hashedPassword: String
)

data class UserLoginResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: RefreshAndAccessTokensDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class PublicJwtResponseDTO(
    @SerializedName("public_key")
    val publicKey: String
)

data class RefreshTokenResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: RefreshAndAccessTokensDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class LogoutResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)

data class GetUserCredentialsResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: UserCredentialsDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class UserByIdRequestDTO(
    @SerializedName("user_id")
    val userId: String
)

enum class UserCredentialsStatusDTO {
    @SerializedName("ACTIVE")
    ACTIVE,

    @SerializedName("DISABLED")
    DISABLED,

    @SerializedName("BANNED")
    BANNED
}

data class UserCredentialsDTO(
    val uuid: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val username: String,
    val status: UserCredentialsStatusDTO,
    @SerializedName("created_at")
    val createdAt: String
)

data class RefreshTokenDTO(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class RefreshAndAccessTokensDTO(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)
