package ru.hse.fandomatch.domain.repos

import ru.hse.fandomatch.domain.model.Token
import ru.hse.fandomatch.domain.model.User

interface GlobalRepository {
    suspend fun getUserInfo(login: String): User?
    suspend fun login(login: String, password: String): Token
    suspend fun register(
        name: String,
        email: String,
        login: String,
        password: String
    ): Token // todo

    suspend fun updateUser(
        name: String? = null,
        surname: String? = null,
        email: String? = null,
        login: String,
        password: String? = null
    )

    suspend fun deleteUser(login: String)
    suspend fun checkPassword(login: String, password: String): Boolean
}
