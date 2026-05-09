package ru.hse.fandomatch.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import ru.hse.fandomatch.data.model.ChangeEmailRequestDTO
import ru.hse.fandomatch.data.model.ChangeEmailResponseDTO
import ru.hse.fandomatch.data.model.ChangePasswordRequestDTO
import ru.hse.fandomatch.data.model.ChangePasswordResponseDTO
import ru.hse.fandomatch.data.model.CheckVerificationCodeRequestDTO
import ru.hse.fandomatch.data.model.CheckVerificationCodeResponseDTO
import ru.hse.fandomatch.data.model.DeleteProfileResponseDTO
import ru.hse.fandomatch.data.model.DeviceTokenRequestDTO
import ru.hse.fandomatch.data.model.FcmTokenResponseDTO
import ru.hse.fandomatch.data.model.GetFcmTokenRequestDTO
import ru.hse.fandomatch.data.model.GetUserCredentialsResponseDTO
import ru.hse.fandomatch.data.model.LogoutResponseDTO
import ru.hse.fandomatch.data.model.PublicJwtResponseDTO
import ru.hse.fandomatch.data.model.RefreshTokenDTO
import ru.hse.fandomatch.data.model.RefreshTokenResponseDTO
import ru.hse.fandomatch.data.model.ResetPasswordRequestDTO
import ru.hse.fandomatch.data.model.ResetPasswordResponseDTO
import ru.hse.fandomatch.data.model.SendVerificationCodeRequestDTO
import ru.hse.fandomatch.data.model.SendVerificationCodeResponseDTO
import ru.hse.fandomatch.data.model.UserByIdRequestDTO
import ru.hse.fandomatch.data.model.UserLoginRequestDTO
import ru.hse.fandomatch.data.model.UserLoginResponseDTO
import ru.hse.fandomatch.data.model.UserRegistrationRequestDTO
import ru.hse.fandomatch.data.model.UserRegistrationResponseDTO

interface UserApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: UserRegistrationRequestDTO
    ): UserRegistrationResponseDTO

    @POST("auth/login")
    suspend fun login(
        @Body request: UserLoginRequestDTO
    ): UserLoginResponseDTO

    @POST("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequestDTO
    ): ChangePasswordResponseDTO

    @POST("auth/logout")
    suspend fun logout(): LogoutResponseDTO

    @GET("users/get-user-credentials")
    suspend fun getUserCredentials(): GetUserCredentialsResponseDTO

    @GET("token/public-jwt")
    suspend fun getPublicJwt(): PublicJwtResponseDTO

    @POST("token/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenDTO
    ): RefreshTokenResponseDTO

    @PUT("users/device-token")
    suspend fun saveDeviceToken(
        @Body request: DeviceTokenRequestDTO
    )

    @POST("users/internal/device-token")
    suspend fun getInternalDeviceToken(
        @Body request: GetFcmTokenRequestDTO,
        @Header("X-API-Key") apiKey: String
    ): FcmTokenResponseDTO

    @POST("users/get-by-id")
    suspend fun getUserById(
        @Body request: UserByIdRequestDTO,
        @Header("X-API-Key") apiKey: String? = null
    ): GetUserCredentialsResponseDTO

    @DELETE("users/profile")
    suspend fun deleteProfile(): DeleteProfileResponseDTO

    @PATCH("users/email")
    suspend fun changeEmail(
        @Body request: ChangeEmailRequestDTO
    ): ChangeEmailResponseDTO

    @POST("auth/verification-code")
    suspend fun sendVerificationCode(
        @Body request: SendVerificationCodeRequestDTO
    ): SendVerificationCodeResponseDTO

    @POST("auth/check-verification-code")
    suspend fun checkVerificationCode(
        @Body request: CheckVerificationCodeRequestDTO
    ): CheckVerificationCodeResponseDTO

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDTO
    ): ResetPasswordResponseDTO
}
