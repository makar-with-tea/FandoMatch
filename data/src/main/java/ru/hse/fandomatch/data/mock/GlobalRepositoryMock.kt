package ru.hse.fandomatch.data.mock

import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository

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
}
