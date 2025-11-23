package ru.hse.fandomatch.domain.model

import java.time.LocalDate

data class User(
    val nickname: String,
    val login: String,
    val email: String,
    val phone: String? = null,
    val fandoms: List<Fandom>,
    val description: String? = null,
    val firstName: String,
    val gender: Gender? = null,
    val passwordHash: String,
    val birthDate: LocalDate,
    val avatarUrl: String? = null,
    val city: City? = null
)

enum class Gender {
    FEMALE,
    MALE,
    NOT_SPECIFIED
}

enum class City {
    // todo real cities
    MOSCOW,
    SAINT_PETERSBURG,
    NOVOSIBIRSK,
    YEKATERINBURG,
    OTHER
}