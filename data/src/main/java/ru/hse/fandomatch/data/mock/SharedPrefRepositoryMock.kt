package ru.hse.fandomatch.data.mock

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class SharedPrefRepositoryMock(): SharedPrefRepository {
    override suspend fun saveUser(username: String) = Unit

    override suspend fun getUser(): String? = null

    override suspend fun clearInfo() = Unit

    override fun saveToken(token: String) = Unit

    override fun getToken(): String? = null

    override fun clearToken() = Unit

    override fun saveRefreshToken(token: String) = Unit

    override fun getRefreshToken(): String? = null

    override fun clearRefreshToken() = Unit

}