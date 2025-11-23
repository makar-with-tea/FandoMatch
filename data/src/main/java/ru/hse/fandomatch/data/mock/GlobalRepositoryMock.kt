package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
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

    companion object {
        val mockFandoms = listOf(
            Fandom(
                id = 1,
                name = "One Piece",
                category = FandomCategory.ANIME,
            ),
            Fandom(
                id = 2,
                name = "Но-Энор",
                category = FandomCategory.OTHER,
            ),
            Fandom(
                id = 3,
                name = "My Chemical Romance",
                category = FandomCategory.MUSIC_GROUP,
            ),
        )
        val mockUser = User(
            nickname = "Johny",
            login = "johndoe",
            email = "johndoe@email.com",
            phone = "88005553535",
            fandoms = mockFandoms,
            description = "just some guy.",
            firstName = "John",
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

    }
}