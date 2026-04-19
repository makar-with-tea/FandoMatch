package ru.hse.fandomatch.data.socket

import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.hse.fandomatch.data.model.ChatPreviewDTO
import ru.hse.fandomatch.data.model.MessageDTO
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType

class ChatSocketServiceImpl(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val wsBaseUrl: String,
) : ChatSocketService {

    override fun observeChatMessages(userId: String): Flow<Message> = callbackFlow {
        val request = Request.Builder()
            .url("$wsBaseUrl/messaging/chats/$userId/ws/messages")
            .build()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val dto = gson.fromJson(text, MessageDTO::class.java)
                    trySend(dto.toDomain())
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        }

        val ws = okHttpClient.newWebSocket(request, listener)
        awaitClose { ws.close(1000, "closed by client") }
    }

    override fun observeChatPreviews(): Flow<ChatPreview> = callbackFlow {
        val request = Request.Builder()
            .url("$wsBaseUrl/messaging/chats/ws/previews")
            .build()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val dto = gson.fromJson(text, ChatPreviewDTO::class.java)
                    trySend(dto.toDomain())
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        }

        val ws = okHttpClient.newWebSocket(request, listener)
        awaitClose { ws.close(1000, "closed by client") }
    }
}
