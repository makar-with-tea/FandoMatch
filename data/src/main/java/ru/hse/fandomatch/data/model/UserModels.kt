package ru.hse.fandomatch.data.model

import com.google.gson.annotations.SerializedName

data class UserRegistrationRequestDTO(
    val email: String,
    val username: String,
    @SerializedName("birth_date")
    val birthDate: Long,
    val name: String,
    @SerializedName("hashed_password")
    val hashedPassword: String,
    @SerializedName("gender")
    val gender: GenderDTO,
    @SerializedName("avatar_media_id")
    val avatarMediaId: String? = null,
)

data class UserRegistrationResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: RefreshAndAccessTokensDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class UserLoginRequestDTO(
    val username: String,
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
    val fcmToken: String,
    @SerializedName("user_id")
    val userId: String? = null
)

data class GetFcmTokenRequestDTO(
    @SerializedName("user_id")
    val userId: String
)

data class FcmTokenResponseDTO(
    @SerializedName("fcm_token")
    val fcmToken: String? = null
)

data class DeleteProfileResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)

data class ChangeEmailRequestDTO(
    @SerializedName("new_email")
    val newEmail: String
)

data class ChangeEmailResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)

data class SendVerificationCodeRequestDTO(
    val email: String
)

data class SendVerificationCodeResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)

data class CheckVerificationCodeRequestDTO(
    val email: String,
    val code: String
)

data class CheckVerificationCodeResponseDTO(
    val status: ResponseStatusDTO,
    val result: Boolean? = null,
    val errorResponse: ErrorDTO? = null
)

data class ResetPasswordRequestDTO(
    val email: String,
    val code: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class ResetPasswordResponseDTO(
    val status: ResponseStatusDTO,
    val errorResponse: ErrorDTO? = null
)
