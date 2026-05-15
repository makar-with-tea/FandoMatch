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
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class ChatSocketServiceImpl(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val wsBaseUrl: String,
    private val sharedPrefRepository: SharedPrefRepository,
) : ChatSocketService {
    
    private var messagesWebSocket: WebSocket? = null
    private var previewsWebSocket: WebSocket? = null

    override fun observeChatMessages(userId: String): Flow<Message> = callbackFlow {
        val request = Request.Builder()
            .url("$wsBaseUrl/messaging/ws")
            .build()

        var ws: WebSocket?

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatSocket", "WebSocket opened for messages (user=$userId), response=${response.code()}")
                ws = webSocket
                sendStompConnect(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("ChatSocket", "WebSocket message received for messages (user=$userId): $text")
                
                val stompFrame = parseStompFrame(text)
                when (stompFrame.command) {
                    "CONNECTED" -> {
                        Log.d("ChatSocket", "STOMP CONNECTED received for messages")
                        val subscribeFrame = buildStompSubscribe(
                            destination = "/user/queue/chats/$userId/messages",
                            id = "sub-messages-$userId"
                        )
                        webSocket.send(subscribeFrame)
                        Log.d("ChatSocket", "Sent STOMP SUBSCRIBE for messages")
                    }
                    "MESSAGE" -> {
                        Log.d("ChatSocket", "STOMP MESSAGE received for messages: ${stompFrame.body}")
                        runCatching {
                            val dto = gson.fromJson(stompFrame.body, MessageDTO::class.java)
                            trySend(dto.toDomain())
                        }
                            .onFailure {
                                Log.e("ChatSocket", "Failed to parse WebSocket message for messages (user=$userId): ${stompFrame.body}", it)
                            }
                            .onSuccess {
                                Log.d("ChatSocket", "WebSocket message parsed successfully for messages (user=$userId)")
                            }
                    }
                    "ERROR" -> {
                        Log.e("ChatSocket", "STOMP ERROR received for messages: ${stompFrame.body}")
                        close(Exception("STOMP ERROR: ${stompFrame.body}"))
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatSocket", "WebSocket failure for messages (user=$userId)", t)
                close(t)
            }
        }

        ws = okHttpClient.newWebSocket(request, listener)
        messagesWebSocket = ws
        awaitClose { 
            Log.d("ChatSocket", "Closing WebSocket for messages (user=$userId)")
            try {
                ws?.close(1000, "closed by client")
            } catch (e: Exception) {
                Log.e("ChatSocket", "Error closing WebSocket for messages", e)
            }
            messagesWebSocket = null
        }
    }

    override fun stopObservingChatMessages() {
        Log.d("ChatSocket", "Stopping chat messages observation")
        try {
            messagesWebSocket?.close(1000, "stopped by client")
        } catch (e: Exception) {
            Log.e("ChatSocket", "Error stopping chat messages WebSocket", e)
        }
        messagesWebSocket = null
    }

    override fun observeChatPreviews(): Flow<ChatPreview> = callbackFlow {
        val request = Request.Builder()
            .url("$wsBaseUrl/messaging/ws")
            .build()

        var ws: WebSocket?

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatSocket", "WebSocket opened for previews, response=${response.code()}")
                ws = webSocket
                sendStompConnect(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("ChatSocket", "WebSocket message received for previews: $text")
                
                val stompFrame = parseStompFrame(text)
                when (stompFrame.command) {
                    "CONNECTED" -> {
                        Log.d("ChatSocket", "STOMP CONNECTED received for previews")
                        val subscribeFrame = buildStompSubscribe(
                            destination = "/user/queue/chat-previews",
                            id = "sub-previews"
                        )
                        webSocket.send(subscribeFrame)
                        Log.d("ChatSocket", "Sent STOMP SUBSCRIBE for previews")
                    }
                    "MESSAGE" -> {
                        Log.d("ChatSocket", "STOMP MESSAGE received for previews: ${stompFrame.body}")
                        runCatching {
                            val dto = gson.fromJson(stompFrame.body, ChatPreviewDTO::class.java)
                            trySend(dto.toDomain())
                        }
                            .onFailure {
                                Log.e("ChatSocket", "Failed to parse WebSocket message for previews: ${stompFrame.body}", it)
                            }
                            .onSuccess {
                                Log.d("ChatSocket", "WebSocket message parsed successfully for previews")
                            }
                    }
                    "ERROR" -> {
                        Log.e("ChatSocket", "STOMP ERROR received for previews: ${stompFrame.body}")
                        close(Exception("STOMP ERROR: ${stompFrame.body}"))
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatSocket", "WebSocket failure for previews", t)
                close(t)
            }
        }

        ws = okHttpClient.newWebSocket(request, listener)
        previewsWebSocket = ws
        awaitClose { 
            Log.d("ChatSocket", "Closing WebSocket for previews")
            try {
                ws?.close(1000, "closed by client")
            } catch (e: Exception) {
                Log.e("ChatSocket", "Error closing WebSocket for previews", e)
            }
            previewsWebSocket = null
        }
    }

    override fun stopObservingChatPreviews() {
        Log.d("ChatSocket", "Stopping chat previews observation")
        try {
            previewsWebSocket?.close(1000, "stopped by client")
        } catch (e: Exception) {
            Log.e("ChatSocket", "Error stopping chat previews WebSocket", e)
        }
        previewsWebSocket = null
    }

    private fun sendStompConnect(webSocket: WebSocket) {
        val connectFrame = buildStompConnect()
        Log.d("ChatSocket", "Sending STOMP CONNECT frame with JWT")
        webSocket.send(connectFrame)
    }

    private fun buildStompConnect(): String {
        val headers = mutableListOf(
            "accept-version" to "1.1,1.2",
            "heart-beat" to "10000,10000"
        )

        sharedPrefRepository.getToken()?.let {
            headers.add("Authorization" to "Bearer $it")
        }
        
        return buildStompFrame(
            command = "CONNECT",
            headers = headers
        )
    }

    private fun buildStompSubscribe(destination: String, id: String): String {
        return buildStompFrame(
            command = "SUBSCRIBE",
            headers = listOf(
                "destination" to destination,
                "id" to id
            )
        )
    }

    private fun buildStompFrame(command: String, headers: List<Pair<String, String>> = emptyList(), body: String = ""): String {
        val sb = StringBuilder()
        sb.append(command).append("\n")
        for ((key, value) in headers) {
            sb.append(key).append(":").append(value).append("\n")
        }
        sb.append("\n")
        if (body.isNotEmpty()) {
            sb.append(body)
        }
        sb.append("\u0000")
        return sb.toString()
    }

    private fun parseStompFrame(text: String): StompFrame {
        val lines = text.split("\n")
        val command = lines.getOrNull(0)?.trim() ?: ""
        
        val headers = mutableMapOf<String, String>()
        var bodyStart = 1
        
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) {
                bodyStart = i + 1
                break
            }
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                headers[parts[0].trim()] = parts[1].trim()
            }
        }
        
        val body = lines.drop(bodyStart)
            .joinToString("\n")
            .trim()
            .removeSuffix("\u0000")
            .trim()
        
        return StompFrame(
            command = command,
            headers = headers,
            body = body
        )
    }

    private data class StompFrame(
        val command: String,
        val headers: Map<String, String>,
        val body: String
    )
}
