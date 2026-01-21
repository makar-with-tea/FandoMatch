package ru.hse.fandomatch.domain.model

data class Filters(
    val genders: List<Gender> = Gender.entries,
    val minAge: Int = 16,
    val maxAge: Int = 80,
    val categories: List<FandomCategory> = listOf(),
    val fandoms: List<Fandom> = listOf(),
    val userCity: City?,
    val onlyInUserCity: Boolean = false,
)
