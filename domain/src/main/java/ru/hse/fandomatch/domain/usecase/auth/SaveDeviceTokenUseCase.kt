package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class SaveDeviceTokenUseCase(
	private val globalRepository: GlobalRepository,
    private val sharedPrefRepository: SharedPrefRepository,
) {
	suspend fun execute(token: String): Result<Unit> = runCatching {
        val userId = sharedPrefRepository.getUserId()
		globalRepository.saveDeviceToken(token, userId)
	}
}
