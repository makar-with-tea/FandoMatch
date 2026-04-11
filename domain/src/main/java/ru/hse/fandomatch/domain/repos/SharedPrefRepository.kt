package ru.hse.fandomatch.domain.repos

interface SharedPrefRepository {
    suspend fun saveUserId(id: String)
    suspend fun getUserId(): String?
    suspend fun clearInfo()
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearRefreshToken()
}
