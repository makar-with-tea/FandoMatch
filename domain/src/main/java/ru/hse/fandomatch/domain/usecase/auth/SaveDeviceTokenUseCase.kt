package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class SaveDeviceTokenUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(token: String): Result<Unit> = runCatching {
		sharedPrefRepository.saveFCMToken(token)
        val userId = sharedPrefRepository.getUserId()
        userId?.let { globalRepository.saveDeviceToken(it, token) }
	}
}
