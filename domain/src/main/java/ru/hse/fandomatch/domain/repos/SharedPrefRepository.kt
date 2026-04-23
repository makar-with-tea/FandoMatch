package ru.hse.fandomatch.domain.repos

interface SharedPrefRepository {
    fun saveUserId(id: String)
    fun getUserId(): String?
    fun clearInfo()
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearRefreshToken()
    fun saveNotificationPermissionShown(shown: Boolean)
    fun getNotificationPermissionShown(): Boolean
}
