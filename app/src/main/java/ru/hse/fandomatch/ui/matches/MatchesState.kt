package ru.hse.fandomatch.ui.matches

import ru.hse.fandomatch.domain.model.ProfileCard

sealed class MatchesState {
    enum class MatchesError {
        IDLE,
        NETWORK,
        NO_PROFILES_FOUND,
    }
    data class Main(
        val profileStack: List<ProfileCard> = emptyList(),
        val isLoading: Boolean = false,
        val error: MatchesError = MatchesError.IDLE,
    ) : MatchesState()
    data object Loading : MatchesState()

    data object Idle : MatchesState()
}

sealed class MatchesEvent {
    data object LoadSuggestedProfiles : MatchesEvent()
    data class LikedProfile(val profileId: String) : MatchesEvent()
    data class DislikedProfile(val profileId: String) : MatchesEvent()
    data class ProfileClicked(val profileId: String) : MatchesEvent()
    data object Clear : MatchesEvent()
}

sealed class MatchesAction {
    data class NavigateToProfile(
        val profileId: String,
    ) : MatchesAction()
}
