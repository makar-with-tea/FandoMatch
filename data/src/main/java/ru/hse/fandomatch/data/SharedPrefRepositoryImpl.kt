package ru.hse.fandomatch.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class SharedPrefRepositoryImpl(
    context: Context
): SharedPrefRepository {
    private val preferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    override suspend fun saveUser(username: String) {
        Log.d("SharedPrefRepository", "saveUser: $username")
        preferences.edit { putString("login", username) }
    }

    override suspend fun getUser(): String? {
//        return preferences.getString("login", null).also {
//            Log.d("SharedPrefRepository", "getUser: $it")
//        }
        return null
    }

    override suspend fun clearInfo() {
        Log.d("SharedPrefRepository", "clearInfo")
        preferences.edit { clear() }
    }

    override fun saveToken(token: String) {
        preferences.edit { putString("auth_token", token) }
    }

    override fun getToken(): String? {
        return preferences.getString("auth_token", null)
    }

    override fun clearToken() {
        preferences.edit { remove("auth_token") }
    }

    override fun saveRefreshToken(token: String) {
        preferences.edit { putString("refresh_token", token) }
    }

    override fun getRefreshToken(): String? {
        return preferences.getString("refresh_token", null)
    }

    override fun clearRefreshToken() {
        preferences.edit { remove("refresh_token") }
    }
}
