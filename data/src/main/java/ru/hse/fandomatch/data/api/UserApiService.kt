package ru.hse.fandomatch.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import ru.hse.fandomatch.data.model.ChangePasswordRequestDTO
import ru.hse.fandomatch.data.model.ChangePasswordResponseDTO
import ru.hse.fandomatch.data.model.DeviceTokenRequestDTO
import ru.hse.fandomatch.data.model.FcmTokenResponseDTO
import ru.hse.fandomatch.data.model.GetFcmTokenRequestDTO
import ru.hse.fandomatch.data.model.GetUserCredentialsResponseDTO
import ru.hse.fandomatch.data.model.LogoutResponseDTO
import ru.hse.fandomatch.data.model.PublicJwtResponseDTO
import ru.hse.fandomatch.data.model.RefreshTokenDTO
import ru.hse.fandomatch.data.model.RefreshTokenResponseDTO
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
    ): Unit

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
}
