package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.exception.NotAuthorizedException
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class RefreshAuthUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun <T> execute(
        block: suspend () -> T,
    ): T {
        return try {
            block()
        } catch (_: NotAuthorizedException) {
            val refreshToken = sharedPrefRepository.getRefreshToken()
            if (refreshToken != null) {
                val newTokens = globalRepository.refreshToken(refreshToken)
                sharedPrefRepository.saveRefreshToken(newTokens.refreshToken)
                sharedPrefRepository.saveToken(newTokens.accessToken)
                sharedPrefRepository.saveUserId(newTokens.userId)
            } else {
                throw IllegalStateException("No auth and no refresh token available")
            }
            block()
        }
    }
}
