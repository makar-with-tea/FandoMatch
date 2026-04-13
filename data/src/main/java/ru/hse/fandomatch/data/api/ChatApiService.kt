package ru.hse.fandomatch.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import ru.hse.fandomatch.data.model.ChatMessagesRequestDTO
import ru.hse.fandomatch.data.model.ChatMessagesResponseDTO
import ru.hse.fandomatch.data.model.ChatPreviewsRequestDTO
import ru.hse.fandomatch.data.model.ChatPreviewsResponseDTO
import ru.hse.fandomatch.data.model.ChatResponseDTO
import ru.hse.fandomatch.data.model.PresignedUploadRequestDTO
import ru.hse.fandomatch.data.model.PresignedUploadResponseDTO
import ru.hse.fandomatch.data.model.SendMessageRequestDTO
import ru.hse.fandomatch.data.model.SendMessageResponseDTO

interface ChatApiService {
    // MEDIA
    @POST("messaging/media/presigned-upload")
    suspend fun getPresignedUploadUrl(
        @Body request: PresignedUploadRequestDTO
    ): PresignedUploadResponseDTO

    // CHATS
    @POST("messaging/chats/previews")
    suspend fun getChatPreviews(
        @Body request: ChatPreviewsRequestDTO
    ): ChatPreviewsResponseDTO

    @GET("messaging/chats/{user_id}")
    suspend fun getChat(
        @Path("user_id") userId: String
    ): ChatResponseDTO

    @POST("messaging/chats/{user_id}/messages")
    suspend fun getChatMessages(
        @Path("user_id") userId: String,
        @Body request: ChatMessagesRequestDTO
    ): ChatMessagesResponseDTO

    @POST("messaging/chats/{user_id}/send")
    suspend fun sendMessage(
        @Path("user_id") userId: String,
        @Body request: SendMessageRequestDTO
    ): SendMessageResponseDTO
}
