package ru.hse.fandomatch.data.mock

import android.util.Log
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GlobalRepositoryMock: GlobalRepository {
    override suspend fun getUserInfo(login: String): User? {
        return mockUser.also {
            Log.d("GlobalRepositoryMock", "getUserInfo: $it")
        }
    }

    override suspend fun login(
        login: String,
        password: String
    ): Token {
        if (login == mockUser.login && password == mockUser.passwordHash) return mockToken.also {
            Log.d("GlobalRepositoryMock", "login: successful for user $login")
        }
        else throw InvalidCredentialsException().also {
            Log.d("GlobalRepositoryMock", "login: failed for user $login")
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        login: String,
        password: String
    ): Token {
        return mockToken.also {
            Log.d("GlobalRepositoryMock", "register: successful for user $login")
        }
    }

    override suspend fun updateUser(
        name: String?,
        surname: String?,
        email: String?,
        login: String,
        password: String?
    ) {
        Log.d("GlobalRepositoryMock", "updateUser: successful for user $login")
    }

    override suspend fun deleteUser(login: String) {
        Log.d("GlobalRepositoryMock", "deleteUser: successful for user $login")
    }

    override suspend fun checkPassword(
        login: String,
        password: String
    ): Boolean {
        return (login == mockUser.login && password == mockUser.passwordHash).also {
            Log.d("GlobalRepositoryMock", "checkPassword: ${if (it) "valid" else "invalid"} for user $login")
        }
    }

    override suspend fun getSuggestedProfiles(userId: Long, size: Int): List<ProfileCard> {
        return mockProfileCards.shuffled().take(size).also {
            Log.d("GlobalRepositoryMock", "getSuggestedProfiles: returned ${it.size} profiles for userId $userId")
        }
    }

    override suspend fun likeProfile(userId: Long, profileId: Long) {
        Log.d("GlobalRepositoryMock", "likeProfile: user $userId liked profile $profileId")
    }

    override suspend fun dislikeProfile(userId: Long, profileId: Long) {
        Log.d("GlobalRepositoryMock", "dislikeProfile: user $userId disliked profile $profileId")
    }
}
