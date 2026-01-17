package ru.hse.fandomatch.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.content.Context

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