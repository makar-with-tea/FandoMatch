package ru.hse.fandomatch.domain.repos

interface SharedPrefRepository {
    fun saveUserId(id: String)
    fun getUserId(): String?
    fun clearUserId()
    fun clearInfo()
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearRefreshToken()
    fun saveNotificationPermissionShown(shown: Boolean)
    fun getNotificationPermissionShown(): Boolean
    fun saveFCMToken(token: String)
    fun getFCMToken(): String?
    fun saveCurrentChatId(chatId: String?)
    fun getCurrentChatId(): String?
}
