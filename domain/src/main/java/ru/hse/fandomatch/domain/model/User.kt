package ru.hse.fandomatch.domain.model

data class User(
    val id: String,
    val fandoms: List<Fandom>,
    val description: String? = null,
    val name: String,
    val gender: Gender,
    val age: Int,
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val city: City? = null,
    val profileType: ProfileType,
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

sealed interface ProfileType {
    data class Own(
        val login: String,
        val email: String,
    ) : ProfileType

    data class Friend(
        val login: String,
    ) : ProfileType

    data class Stranger(
        val hasCurrentUserReacted: Boolean,
    ) : ProfileType
}

data class OtherProfileItem(
    val id: String,
    val name: String,
    val login: String?,
    val avatarUrl: String?,
)
