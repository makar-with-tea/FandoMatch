package ru.hse.fandomatch.data

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.repos.MediaRepository
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MediaRepositoryImpl(
    private val context: Context,
) : MediaRepository {
    override suspend fun downloadMediaToGallery(
        mediaUrl: String,
        mediaType: MediaType,
    ) {
        val uri = downloadMediaToGallery(
            context = context,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
        )

        if (uri == null) {
            throw IllegalStateException("Failed to save media to gallery")
        }
    }

    private suspend fun downloadMediaToGallery(
        context: Context,
        mediaUrl: String,
        mediaType: MediaType,
    ): Uri? = withContext(Dispatchers.IO) {
        val connection = (URL(mediaUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            requestMethod = "GET"
        }

        try {
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP $responseCode while downloading media")
            }

            val mimeType = connection.contentType
                ?.substringBefore(';')
                ?.takeIf { it.isNotBlank() }
                ?: defaultMimeType(mediaType)

            val bytes = connection.inputStream.use { it.readBytes() }
            saveMediaBytes(context, bytes, mimeType, mediaType)
        } finally {
            connection.disconnect()
        }
    }

    private fun saveMediaBytes(
        context: Context,
        bytes: ByteArray,
        mimeType: String,
        mediaType: MediaType,
    ): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(context, bytes, mimeType, mediaType)
        } else {
            saveToLegacyPublicDirectory(context, bytes, mimeType, mediaType)
        }
    }

    private fun saveToMediaStore(
        context: Context,
        bytes: ByteArray,
        mimeType: String,
        mediaType: MediaType,
    ): Uri? {
        val resolver = context.contentResolver
        val collection = when (mediaType) {
            MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val fileName = buildFileName(mediaType, mimeType)
        val relativePath = when (mediaType) {
            MediaType.IMAGE -> Environment.DIRECTORY_PICTURES + File.separator + "FandoMatch"
            MediaType.VIDEO -> Environment.DIRECTORY_MOVIES + File.separator + "FandoMatch"
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, values) ?: return null
        resolver.openOutputStream(uri)?.use { output ->
            output.write(bytes)
            output.flush()
        } ?: return null

        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return uri
    }

    private fun saveToLegacyPublicDirectory(
        context: Context,
        bytes: ByteArray,
        mimeType: String,
        mediaType: MediaType,
    ): Uri? {
        if (!hasLegacyStoragePermission(context)) {
            throw SecurityException("WRITE_EXTERNAL_STORAGE permission is required on Android 9 and below")
        }

        val directoryType = when (mediaType) {
            MediaType.IMAGE -> Environment.DIRECTORY_PICTURES
            MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
        }
        val folder = File(
            Environment.getExternalStoragePublicDirectory(directoryType),
            "FandoMatch"
        ).apply { mkdirs() }

        val file = File(folder, buildFileName(mediaType, mimeType))
        FileOutputStream(file).use { output ->
            output.write(bytes)
            output.flush()
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf(mimeType),
            null
        )

        return Uri.fromFile(file)
    }

    private fun hasLegacyStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun defaultMimeType(mediaType: MediaType): String = when (mediaType) {
        MediaType.IMAGE -> "image/jpeg"
        MediaType.VIDEO -> "video/mp4"
    }

    private fun buildFileName(mediaType: MediaType, mimeType: String): String {
        val extension = mimeType
            .extensionFromMimeType()
            ?: when (mediaType) {
                MediaType.IMAGE -> "jpg"
                MediaType.VIDEO -> "mp4"
            }

        val prefix = when (mediaType) {
            MediaType.IMAGE -> "image"
            MediaType.VIDEO -> "video"
        }

        return "${prefix}_${System.currentTimeMillis()}.$extension"
    }

    private fun String.extensionFromMimeType(): String? {
        return when (lowercase(Locale.ROOT)) {
            "image/jpeg" -> "jpg"
            "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "video/mp4" -> "mp4"
            "video/quicktime" -> "mov"
            "video/webm" -> "webm"
            else -> null
        }
    }
}

