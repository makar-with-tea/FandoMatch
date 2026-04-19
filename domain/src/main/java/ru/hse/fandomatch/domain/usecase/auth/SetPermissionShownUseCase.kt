package ru.hse.fandomatch.domain.usecase.auth

import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class SetPermissionShownUseCase(
    private val sharedPrefRepository: SharedPrefRepository,
) {
    fun execute(shown: Boolean) {
        sharedPrefRepository.saveNotificationPermissionShown(shown)
    }
}
