package ru.hse.fandomatch.domain.model

data class ProfileCard(
    val id: Long,
    val fandoms: List<Fandom>,
    val description: String? = null,
    val name: String,
    val gender: Gender? = null,
    val avatarUrl: String? = null,
    val age: Int,
    val compatibilityPercentage: Int,
)
