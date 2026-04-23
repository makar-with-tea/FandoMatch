package ru.hse.fandomatch.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.hse.fandomatch.MainActivity
import ru.hse.fandomatch.R

class FandoMatchMessagingService(): FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i("FCM", "Message data payload: ${message.data}")
        val type = when (message.data[TYPE]) {
            NotificationType.CHAT.rawValue -> NotificationType.CHAT
            NotificationType.MATCH.rawValue -> NotificationType.MATCH
            else -> return
        }
        val id = when(type) {
            NotificationType.CHAT -> message.data[CHAT_ID]
            NotificationType.MATCH -> message.data[USER_ID]
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
            putExtra(ID, id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val requestCode = "$type:$id".hashCode()
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

        Log.i("FCM", "message shown: ${message.data}")
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        // todo
//        sendRegistrationToServer(token)
    }
}

private enum class NotificationType(val rawValue: String) {
    CHAT("chat"), MATCH("match")
}

private const val USER_ID = "userId"
private const val CHAT_ID = "chatId"
private const val ID = "id"
private const val NAVIGATE_TO = "navigateTo"
private const val TYPE = "type"
private const val NAME = "name"