package ru.hse.fandomatch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.ui.theme.CustomColors
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale

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

fun nameAndAgeString(name: String, age: Int): String = "$name, $age"

fun timestampToTimeAgo(timestamp: Long, context: Context) : String {
    val dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC)
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

fun Long.timestampToDateString(): String {
    val dateTime = LocalDateTime.ofEpochSecond(this / 1000, 0, ZoneOffset.UTC)
    return String.format("%02d.%02d.%04d", dateTime.dayOfMonth, dateTime.monthValue, dateTime.year)
}

fun Gender.stringId(): Int = when (this) {
    Gender.MALE -> R.string.male_gender
    Gender.FEMALE -> R.string.female_gender
    Gender.NOT_SPECIFIED -> R.string.unspecified_gender
}

fun FandomCategory.toStringId(): Int = when (this) {
    FandomCategory.ANIME_MANGA -> R.string.fandom_category_anime_manga
    FandomCategory.BOOKS -> R.string.fandom_category_books
    FandomCategory.CARTOONS -> R.string.fandom_category_cartoons
    FandomCategory.FILMS -> R.string.fandom_category_films
    FandomCategory.TV_SERIES -> R.string.fandom_category_tv_series
    FandomCategory.GAMES -> R.string.fandom_category_games
    FandomCategory.TABLETOP_GAMES -> R.string.fandom_category_tabletop_games
    FandomCategory.MUSIC -> R.string.fandom_category_music
    FandomCategory.THEATER_MUSICALS -> R.string.fandom_category_theater_musicals
    FandomCategory.PODCASTS -> R.string.fandom_category_podcasts
    FandomCategory.COMICS -> R.string.fandom_category_comics
    FandomCategory.CONTENT_CREATORS -> R.string.fandom_category_content_creators
    FandomCategory.CELEBRITIES -> R.string.fandom_category_celebrities
    FandomCategory.SPORTS -> R.string.fandom_category_sports
    FandomCategory.HISTORY -> R.string.fandom_category_history
    FandomCategory.MYTHOLOGY -> R.string.fandom_category_mythology
    FandomCategory.OTHER -> R.string.fandom_category_other
}

fun City.getName(): String {
    val locale = Locale.getDefault().language
    return when (locale) {
        "ru" -> this.nameRussian
        else -> this.nameEnglish
    }
}

private const val LATIN = "abcdefghijklmnopqrstuvwxyz"
private const val LOGIN_SPECIAL_SYMBOLS = "_-"
private const val SPECIAL_SYMBOLS = "!@#$%^*()-_"
fun String.checkNameLength() = this.length in 2..20
fun String.checkNameContent() = this.all { it.isLetter() || it == ' ' || it == '\'' }
fun String.checkEmailContent() = this.isNotEmpty() && "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex().matches(this)
fun String.checkLoginLength() = this.length in 5..15
fun String.checkLoginContent() = this.all { it.isDigit() || it.lowercase() in LATIN || it in LOGIN_SPECIAL_SYMBOLS }
fun String.checkDescriptionLength() = this.length <= 500
fun String.checkPasswordLength() = this.length >= 8
fun String.checkPasswordContent() = this.any { it.isDigit() }
        && this.any { it.lowercase() in LATIN }
        && this.any { it !in LATIN && !it.isDigit() }
        && this.all { it.isLetterOrDigit() || it in SPECIAL_SYMBOLS }
fun String.checkFandomNameLength() = this.length in 2..100
fun String.checkPostContentLength() = this.length <= 2000 // todo какая длина?

@Composable
fun FandomCategory.getColor(): Color {
    return when (this) {
        FandomCategory.ANIME_MANGA -> CustomColors.animeMangaBackground
        FandomCategory.BOOKS -> CustomColors.booksBackground
        FandomCategory.CARTOONS -> CustomColors.cartoonsBackground
        FandomCategory.FILMS -> CustomColors.filmsBackground
        FandomCategory.TV_SERIES -> CustomColors.tvSeriesBackground
        FandomCategory.GAMES -> CustomColors.gamesBackground
        FandomCategory.TABLETOP_GAMES -> CustomColors.tabletopGamesBackground
        FandomCategory.MUSIC -> CustomColors.musicBackground
        FandomCategory.COMICS -> CustomColors.comicsBackground
        FandomCategory.THEATER_MUSICALS -> CustomColors.theaterMusicalsBackground
        FandomCategory.PODCASTS -> CustomColors.podcastsBackground
        FandomCategory.CONTENT_CREATORS -> CustomColors.contentCreatorsBackground
        FandomCategory.CELEBRITIES -> CustomColors.celebritiesBackground
        FandomCategory.SPORTS -> CustomColors.sportsBackground
        FandomCategory.HISTORY -> CustomColors.historyBackground
        FandomCategory.MYTHOLOGY -> CustomColors.mythologyBackground
        FandomCategory.OTHER -> CustomColors.otherBackground
    }
}

fun getBytesFromUri(context: Context, uri: Uri): ByteArray? {
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        return inputStream.readBytes()
    }
    return null
}

const val MAX_NUMBER_OF_ATTACHMENTS = 5