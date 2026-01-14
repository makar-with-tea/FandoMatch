package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.repos.GlobalRepository
import java.time.LocalDate

class GlobalRepositoryMock: GlobalRepository {
    override suspend fun getUserInfo(login: String): User? {
        return if (login == mockUser.login) mockUser else null
    }

    override suspend fun login(
        login: String,
        password: String
    ): Token {
        if (login == mockUser.login && password == mockUser.passwordHash) return mockToken
        else throw InvalidCredentialsException()
    }

    override suspend fun register(
        name: String,
        email: String,
        login: String,
        password: String
    ): Token {
        return mockToken
    }

    override suspend fun updateUser(
        name: String?,
        surname: String?,
        email: String?,
        login: String,
        password: String?
    ) = Unit

    override suspend fun deleteUser(login: String) = Unit

    override suspend fun checkPassword(
        login: String,
        password: String
    ): Boolean {
        return login == mockUser.login && password == mockUser.passwordHash
    }

    override suspend fun getSuggestedProfiles(userId: Long, size: Int): List<ProfileCard> {
        return mockProfileCards.shuffled().take(size)
    }

    override suspend fun likeProfile(userId: Long, profileId: Long) = Unit

    override suspend fun dislikeProfile(userId: Long, profileId: Long) = Unit

    companion object {
        val mockFandoms = listOf(
            Fandom(
                id = 1,
                name = "One Piece",
                category = FandomCategory.ANIME_MANGA,
            ),
            Fandom(
                id = 2,
                name = "Но-Энор",
                category = FandomCategory.OTHER,
            ),
            Fandom(
                id = 3,
                name = "My Chemical Romance",
                category = FandomCategory.MUSIC,
            ),
            Fandom(
                id = 4,
                name = "Ведьмак",
                category = FandomCategory.BOOKS,
            ),
            Fandom(
                id = 5,
                name = "Утиные истории",
                category = FandomCategory.CARTOONS,
            )
        )
        val mockUser = User(
            id = 1,
            nickname = "Johny",
            login = "johndoe",
            email = "johndoe@email.com",
            phone = "88005553535",
            fandoms = mockFandoms,
            description = "just some guy.",
            name = "John",
            gender = Gender.MALE,
            passwordHash = "password1",
            birthDate = LocalDate.of(1990, 5, 20),
            avatarUrl = null,
            city = City.MOSCOW,
        )

        val mockToken = Token(
            accessToken = "accessToken",
            refreshToken = "refreshToken",
        )

        val mockProfileCards = listOf(
            ProfileCard(
                id = 12345,
                fandoms = mockFandoms,
                description = "Я люблю аниме и музыку :3",
                name = "Алиса",
                gender = Gender.FEMALE,
                avatarUrl = "luffy",
                age = 21,
                compatibilityPercentage = 95,
            ),
            ProfileCard(
                id = 67890,
                fandoms = listOf(mockFandoms[0]),
                description = "Just a pirate in search of adventure.",
                name = "Bob",
                gender = Gender.MALE,
                avatarUrl = "peace_was_never_an_option",
                age = null,
                compatibilityPercentage = 80,
            ),
            ProfileCard(
                id = 11223,
                fandoms = listOf(mockFandoms[2]),
                description = "Music is life.",
                name = "Charlie",
                gender = Gender.NOT_SPECIFIED,
                avatarUrl = "pet_the_forbidden_dog",
                age = 25,
                compatibilityPercentage = 70,
            ),
            ProfileCard(
                id = 44556,
                fandoms = listOf(),
                description = "Just a mysterious person.",
                name = "Dana",
                gender = Gender.FEMALE,
                avatarUrl = null,
                age = 30,
                compatibilityPercentage = 60,
            ),
        )

    }
}
