package ru.hse.fandomatch.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ru.hse.fandomatch.R
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

fun Boolean?.orFalse(): Boolean = this ?: false

fun Int?.orZero(): Int = this ?: 0

fun <T> T?.orDefault(default: T): T = this ?: default

class BitmapHelper {
    companion object {
        fun bitmapToByteArray(bitmap: Bitmap?): ByteArray? {
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream) ?: return null
            return stream.toByteArray()
        }

        fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
            byteArray ?: return null
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
    }
}

fun rawResId(name: String, context: Context): Int {
    return context.resources.getIdentifier(name, "raw", context.packageName)
}

fun nameAndAgeString(name: String, age: Int?): String = "$name${age?.let { ", $it" }.orEmpty()}"

fun timestampToTimeAgo(timestamp: Long, context: Context) : String {
    val dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC)
    val secondsAgo = (System.currentTimeMillis() - timestamp) / 1000
    val minutesAgo = secondsAgo / 60
    val hoursAgo = minutesAgo / 60
    val daysAgo = hoursAgo / 24
    val weeksAgo = daysAgo / 7
    val monthsAgo = daysAgo / 30
    val yearsAgo = daysAgo / 365

    return when {
        yearsAgo > 0 -> "$yearsAgo ${context.resources.getString(R.string.last_message_time_years)}"
        monthsAgo > 0 -> "$monthsAgo ${context.resources.getString(R.string.last_message_time_months)}"
        weeksAgo > 0 -> "$weeksAgo ${context.resources.getString(R.string.last_message_time_weeks)}"
        daysAgo > 0 -> "$daysAgo ${context.resources.getString(R.string.last_message_time_days)}"
        else -> String.format("%02d:%02d", dateTime.hour, dateTime.minute)
    }
}