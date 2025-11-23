package ru.hse.fandomatch.domain.model

data class Fandom(
    val id: Int,
    val name: String,
    val category: FandomCategory
)

// todo real categories
enum class FandomCategory {
    ANIME,
    BOOK,
    FILM,
    GAME,
    MUSIC_GROUP,
    OTHER
}
