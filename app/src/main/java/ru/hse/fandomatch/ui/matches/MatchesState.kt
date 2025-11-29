package ru.hse.fandomatch.ui.matches

sealed class MatchesState {
    enum class MatchesError {
        IDLE
    }
    data class Main(
        // todo type ProfileCardModel or smth
//        val profileStack: List<ProfileCardModel> = emptyList(),
        val isLoading: Boolean = false
    ) : MatchesState()
    data object Loading : MatchesState()
}

sealed class MatchesEvent {
    data object LoadMatches : MatchesEvent()
    data class LikeProfile(val profileId: Int) : MatchesEvent()
    data class DislikeProfile(val profileId: Int) : MatchesEvent()
}

sealed class AuthorizationAction {
    data object NavigateToMatches : AuthorizationAction()
}
