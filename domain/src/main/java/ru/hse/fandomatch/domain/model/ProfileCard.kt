package ru.hse.fandomatch.domain.model

data class ProfileCard(
    val id: String,
    val fandoms: List<Fandom>,
    val description: String? = null,
    val name: String,
    val gender: Gender,
    val avatar: MediaItem? = null,
    val age: Int,
    val city: City?,
    val compatibilityPercentage: Int,
)
