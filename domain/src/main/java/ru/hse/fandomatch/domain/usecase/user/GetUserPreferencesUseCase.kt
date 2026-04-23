package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.UserPreferences
import ru.hse.fandomatch.domain.repos.GlobalRepository

class GetUserPreferencesUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(): Result<UserPreferences> = runCatching {
        globalRepository.getUserPreferences()
    }
}