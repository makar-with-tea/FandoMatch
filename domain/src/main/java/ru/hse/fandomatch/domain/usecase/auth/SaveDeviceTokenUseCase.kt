package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.GlobalRepository

class SaveDeviceTokenUseCase(
	private val globalRepository: GlobalRepository,
) {
	suspend fun execute(token: String): Result<Unit> = runCatching {
		globalRepository.saveDeviceToken(token)
	}
}
