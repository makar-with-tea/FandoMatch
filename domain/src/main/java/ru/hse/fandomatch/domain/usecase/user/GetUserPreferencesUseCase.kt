package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.model.UserPreferences
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.auth.RefreshAuthUseCase

class GetUserPreferencesUseCase(
    private val globalRepository: GlobalRepository,
    private val refreshAuthUseCase: RefreshAuthUseCase,
) {
    suspend fun execute(): Result<UserPreferences> = runCatching {
        refreshAuthUseCase.execute {
            globalRepository.getUserPreferences()
        }
    }
}
