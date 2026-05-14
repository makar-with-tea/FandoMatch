package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class SaveDeviceTokenUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
) {
    fun execute(token: String) {
		sharedPrefRepository.saveFCMToken(token)
	}
}
