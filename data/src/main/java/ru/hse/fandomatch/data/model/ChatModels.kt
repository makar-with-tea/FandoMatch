package ru.hse.fandomatch.data.model

import com.google.gson.annotations.SerializedName

enum class MediaTypeDTO {
    @SerializedName("IMAGE")
    IMAGE,

    @SerializedName("VIDEO")
    VIDEO
}

data class MediaItemDTO(
    @SerializedName("media_id")
    val mediaId: String,
    @SerializedName("media_type")
    val mediaType: MediaTypeDTO,
    val url: String
)

data class PresignedUploadRequestDTO(
    @SerializedName("media_type")
    val mediaType: MediaTypeDTO
)

data class PresignedUploadDataDTO(
    @SerializedName("media_id")
    val mediaId: String,
    @SerializedName("upload_url")
    val uploadUrl: String,
    @SerializedName("expires_at")
    val expiresAt: Long
)

data class PresignedUploadResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: PresignedUploadDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class ChatDTO(
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("participant_id")
    val participantId: String,
    @SerializedName("participant_name")
    val participantName: String,
    @SerializedName("participant_avatar_url")
    val participantAvatarUrl: String? = null
)

data class ChatPreviewDTO(
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("participant_name")
    val participantName: String,
    @SerializedName("participant_avatar_url")
    val participantAvatarUrl: String? = null,
    @SerializedName("last_message")
    val lastMessage: String,
    @SerializedName("is_last_message_from_this_user")
    val isLastMessageFromThisUser: Boolean,
    @SerializedName("last_message_timestamp")
    val lastMessageTimestamp: Long,
    @SerializedName("new_messages_count")
    val newMessagesCount: Int
)

data class MessageDTO(
    @SerializedName("message_id")
    val messageId: String,
    @SerializedName("is_from_this_user")
    val isFromThisUser: Boolean,
    val content: String,
    val timestamp: Long,
    @SerializedName("media_items")
    val mediaItems: List<MediaItemDTO>? = null
)

data class ChatPreviewsRequestDTO(
    @SerializedName("before_timestamp")
    val beforeTimestamp: Long? = null,
    val size: Int
)

data class ChatPreviewsDataDTO(
    val previews: List<ChatPreviewDTO>
)

data class ChatPreviewsResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: ChatPreviewsDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class ChatResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: ChatDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class ChatMessagesRequestDTO(
    @SerializedName("before_timestamp")
    val beforeTimestamp: Long? = null,
    val size: Int
)

data class ChatMessagesDataDTO(
    val messages: List<MessageDTO>
)

data class ChatMessagesResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: ChatMessagesDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)

data class SendMessageRequestDTO(
    val content: String,
    @SerializedName("media_ids")
    val mediaIds: List<String>? = null,
    val timestamp: Long
)

data class SendMessageDataDTO(
    @SerializedName("message_id")
    val messageId: String
)

data class SendMessageResponseDTO(
    val status: ResponseStatusDTO,
    val successResponse: SendMessageDataDTO? = null,
    val errorResponse: ErrorDTO? = null
)
