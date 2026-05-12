package ru.hse.fandomatch.data.socket

import android.util.Log
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
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatSocket", "WebSocket opened for messages (user=$userId), response=${response.code()}")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("ChatSocket", "WebSocket message received for messages (user=$userId): $text")
                runCatching {
                    val dto = gson.fromJson(text, MessageDTO::class.java)
                    trySend(dto.toDomain())
                }
                    .onFailure {
                        Log.e("ChatSocket", "Failed to parse WebSocket message for messages (user=$userId): $text", it)
                    }
                    .onSuccess {
                        Log.d("ChatSocket", "WebSocket message parsed successfully for messages (user=$userId): $text")
                    }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatSocket", "WebSocket failure for messages (user=$userId)", t)
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
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatSocket", "WebSocket opened for previews, response=${response.code()}")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val dto = gson.fromJson(text, ChatPreviewDTO::class.java)
                    trySend(dto.toDomain())
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatSocket", "WebSocket failure for previews", t)
                close(t)
            }
        }

        val ws = okHttpClient.newWebSocket(request, listener)
        awaitClose { ws.close(1000, "closed by client") }
    }
}
