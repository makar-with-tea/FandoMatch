package ru.hse.fandomatch.data.model

import com.google.gson.annotations.SerializedName

data class UserRegistrationRequestDTO(
    val email: String,
    val username: String,
    @SerializedName("birth_date")
    val birthDate: Long,
    val name: String,
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
    val username: String,
    val status: UserCredentialsStatusDTO,
    @SerializedName("created_at")
    val createdAt: String
)

data class RefreshTokenDTO(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class ChangePasswordRequestDTO(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class ChangePasswordResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)

data class RefreshAndAccessTokensDTO(
    val uuid: String,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class DeviceTokenRequestDTO(
    @SerializedName("fcm_token")
    val fcmToken: String
)

data class GetFcmTokenRequestDTO(
    @SerializedName("user_id")
    val userId: String
)

data class FcmTokenResponseDTO(
    @SerializedName("fcm_token")
    val fcmToken: String? = null
)
