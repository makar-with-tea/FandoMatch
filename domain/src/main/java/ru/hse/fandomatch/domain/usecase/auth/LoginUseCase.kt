package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class LoginUseCase(
    private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository
) {
    suspend fun execute(login: String, password: String) : Result<Unit> {
        return runCatching {
            val res = globalRepository.login(login, password)
            sharedPrefRepository.saveUserId(res.userId)
            sharedPrefRepository.saveToken(res.accessToken)
            sharedPrefRepository.saveRefreshToken(res.refreshToken)
        }
    }
}
