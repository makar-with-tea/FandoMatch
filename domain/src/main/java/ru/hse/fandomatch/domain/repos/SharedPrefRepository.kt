package ru.hse.fandomatch.domain.repos

interface SharedPrefRepository {
    suspend fun saveUser(username: String)
    suspend fun getUser(): String?
    suspend fun clearInfo()
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearRefreshToken()
}
