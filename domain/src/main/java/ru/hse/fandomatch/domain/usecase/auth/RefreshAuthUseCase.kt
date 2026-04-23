package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.exception.AuthRefreshRequiredException
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class RefreshAuthUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
    suspend fun <T> execute(
        block: suspend () -> T,
    ): Result<T> = runCatching {
        try {
            block()
        } catch (_: AuthRefreshRequiredException) {
            val refreshToken = sharedPrefRepository.getRefreshToken()
            if (refreshToken != null) {
                val newTokens = globalRepository.refreshToken(refreshToken)
                sharedPrefRepository.saveRefreshToken(newTokens.refreshToken)
                sharedPrefRepository.saveToken(newTokens.accessToken)
                sharedPrefRepository.saveUserId(newTokens.userId)
            } else {
                throw IllegalStateException("No refresh token available")
            }
            block()
        }
    }
}
