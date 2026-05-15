package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.exception.NotAuthorizedException
import ru.hse.fandomatch.domain.logging.Logger
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class RefreshAuthUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
    private val logger: Logger,
) {
    suspend fun <T> execute(
        block: suspend () -> T,
    ): T {
        return try {
            block()
        } catch (_: NotAuthorizedException) {
            logger.i("RefreshAuthUseCase","Got NotAuthorizedException, trying to refresh token")
            sharedPrefRepository.clearToken()
            sharedPrefRepository.clearUserId()
            val refreshToken = sharedPrefRepository.getRefreshToken()
            sharedPrefRepository.clearRefreshToken()
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
