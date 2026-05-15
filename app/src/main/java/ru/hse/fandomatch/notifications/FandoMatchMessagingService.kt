package ru.hse.fandomatch.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.hse.fandomatch.MainActivity
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.logging.Logger
import ru.hse.fandomatch.domain.usecase.auth.SaveDeviceTokenUseCase
import ru.hse.fandomatch.domain.usecase.chat.GetCurrentChatIdUseCase

class FandoMatchMessagingService : FirebaseMessagingService(), KoinComponent {
    private val saveDeviceTokenUseCase: SaveDeviceTokenUseCase by inject()
    private val getCurrentChatIdUseCase: GetCurrentChatIdUseCase by inject()
    private val logger: Logger by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        logger.d("FCM", "Message data payload: ${message.data}")
        val type = when (message.data[TYPE]) {
            NotificationType.CHAT.rawValue -> NotificationType.CHAT
            NotificationType.MATCH.rawValue -> NotificationType.MATCH
            else -> return
        }
        val userId = message.data[USER_ID]
        if (type == NotificationType.CHAT) {
            val currentChatId = getCurrentChatIdUseCase.execute()
            if (currentChatId != null && currentChatId == userId) {
                logger.d("FCM", "Notification ignored, user is currently in chat with user $currentChatId")
                return
            } else {
                logger.d("FCM", "current chat with $currentChatId, message from $userId")
            }
        }

        val title = resources.getString(
            when (type) {
                NotificationType.CHAT -> R.string.notification_title_new_message
                NotificationType.MATCH -> R.string.notification_title_new_match
            }
        )
        val name = message.data[NAME] ?: ""
        val content = resources.getString(
            when (type) {
                NotificationType.CHAT -> R.string.notification_message_new_message
                NotificationType.MATCH -> R.string.notification_message_new_match
            },
            name
        )
        val iconRes = when(type) {
            NotificationType.CHAT -> R.drawable.ic_message
            NotificationType.MATCH -> R.drawable.ic_cards_stack_star
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(NAVIGATE_TO, type.rawValue)
            putExtra(USER_ID, userId)
            logger.d("FCM", "Creating intent with navigateTo: ${type.rawValue}, userId: $userId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val requestCode = "$type:$userId".hashCode()
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(iconRes)
            .setColor(resources.getColor(R.color.notification_icon_color, theme))
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(requestCode, notification)

        logger.d("FCM", "message shown: ${message.data}")
    }

    override fun onNewToken(token: String) {
        logger.d("FCM", "Refreshed token: $token")
        serviceScope.launch(Dispatchers.IO) {
            saveDeviceTokenUseCase.execute(token)
                .onSuccess {
                    logger.d("FCM", "Device token saved successfully")
                }
                .onFailure { e ->
                    logger.e("FCM", "Failed to save device token", e)
                }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

private enum class NotificationType(val rawValue: String) {
    CHAT("chat"), MATCH("match")
}

private const val USER_ID = "userId"
private const val NAVIGATE_TO = "navigateTo"
private const val TYPE = "type"
private const val NAME = "name"