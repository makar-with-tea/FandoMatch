package ru.hse.fandomatch.domain.usecase.user

import ru.hse.fandomatch.domain.repos.GlobalRepository

class UpdateUserPreferencesUseCase(
    private val globalRepository: GlobalRepository,
) {
    suspend fun execute(
        matchNotificationsEnabled: Boolean,
        messageNotificationsEnabled: Boolean,
        hideMyPostsFromNonMatches: Boolean
    ): Result<Unit> = runCatching {
        globalRepository.updateUserPreferences(
            matchNotificationsEnabled = matchNotificationsEnabled,
            messageNotificationsEnabled = messageNotificationsEnabled,
            hideMyPostsFromNonMatches = hideMyPostsFromNonMatches
        )
    }
}