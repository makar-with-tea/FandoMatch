package ru.hse.fandomatch.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    @POST("users/get-by-id")
    suspend fun getUserById(
        @Body request: UserByIdRequestDTO,
        @Header("X-API-Key") apiKey: String? = null
    ): GetUserCredentialsResponseDTO
}
