package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class GetPermissionShownUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
) {
    fun execute(): Boolean {
        return sharedPrefRepository.getNotificationPermissionShown()
    }
}