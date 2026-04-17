package ru.hse.fandomatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class CustomColors {
    companion object {
        @Composable
        fun getColorForTheme(lightColor: Color, darkColor: Color): Color {
            return if (isSystemInDarkTheme()) {
                darkColor
            } else {
                lightColor
            }
        }

        val animeMangaBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFF8E1E7),
                darkColor = Color(0xFF4A2C34)
            )
        val booksBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFFDE9D9), // Soft Peach
                darkColor = Color(0xFF5A3A2A)  // Warm Brown
            )
        val cartoonsBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFFFF4D9),
                darkColor = Color(0xFF5A4A2A)
            )
        val filmsBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFE8F5E9),
                darkColor = Color(0xFF2E4A2E)
            )
        val tvSeriesBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFD9F2F8),
                darkColor = Color(0xFF2A4A5A)
            )
        val gamesBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFD9E8F8),
                darkColor = Color(0xFF2A3A5A)
            )
        val tabletopGamesBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFE8D9F8),
                darkColor = Color(0xFF3A2A5A)
            )
        val musicBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFF8D9E8),
                darkColor = Color(0xFF5A2A4A)
            )
        val comicsBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFF0E0D6),
                darkColor = Color(0xFF4A3A2A)
            )
        val theaterMusicalsBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFE0F7FA),
                darkColor = Color(0xFF2A4A4A)
            )
        val podcastsBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFF1E4F3),
                darkColor = Color(0xFF4A2A4A)
            )

        val celebritiesBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFE8F8D9),
                darkColor = Color(0xFF3A5A2A)
            )
        val sportsBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFD9F8E8),
                darkColor = Color(0xFF2A5A3A)
            )
        val historyBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFD9E8F8),
                darkColor = Color(0xFF2A3A5A)
            )
        val mythologyBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFF8D9F0),
                darkColor = Color(0xFF5A2A3A)
            )
        val otherBackground: Color
            @Composable get() = getColorForTheme(
                lightColor = Color(0xFFE8E8E8),
                darkColor = Color(0xFF3A3A3A)
            )
    }
}
