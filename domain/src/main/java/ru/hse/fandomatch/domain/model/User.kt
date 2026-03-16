package ru.hse.fandomatch.domain.model

import java.time.LocalDate

data class User(
    val id: Long,
    val login: String,
    val email: String,
    val phone: String? = null,
    val fandoms: List<Fandom>,
    val description: String? = null,
    val name: String,
    val gender: Gender? = null,
    val birthDate: LocalDate,
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val city: City? = null
)

enum class Gender {
    FEMALE,
    MALE,
    NOT_SPECIFIED
}

data class City(
    val nameRussian: String,
    val nameEnglish: String,
)

data class UserPreferences(
    val matchesEnabled: Boolean,
    val messagesEnabled: Boolean,
    val hideMyPostsFromNonMatches: Boolean,
)
