package ru.hse.fandomatch.domain.model

data class Fandom(
    val id: Int,
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
    CONTENT_CREATORS,
    CELEBRITIES,
    SPORTS,
    HISTORY,
    MYTHOLOGY,
    OTHER
}

