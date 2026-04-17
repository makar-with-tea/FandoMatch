package ru.hse.fandomatch.domain.model

data class Fandom(
    val id: String,
    val name: String,
    val category: FandomCategory
)

enum class FandomCategory {
    ANIME_MANGA,
    BOOKS,
    CARTOONS,
    FILMS,
    TV_SERIES,
    GAMES,
    TABLETOP_GAMES,
    MUSIC,
    THEATER_MUSICALS,
    PODCASTS,
    COMICS,
    CELEBRITIES,
    SPORTS,
    HISTORY,
    MYTHOLOGY,
    OTHER
}
