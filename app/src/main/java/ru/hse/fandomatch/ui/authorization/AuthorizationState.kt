package ru.hse.fandomatch.ui.authorization

sealed class AuthorizationState {
    enum class AuthorizationError {
        EMPTY_LOGIN,
        EMPTY_PASSWORD,
        INVALID_CREDENTIALS,
        NETWORK,
        IDLE
    }
    data class Main(
        val login: String = "",
        val password: String = "",
        val passwordVisibility: Boolean = false,
        val loginError: AuthorizationError = AuthorizationError.IDLE,
        val passwordError: AuthorizationError = AuthorizationError.IDLE,
        val isLoading: Boolean = false
    ) : AuthorizationState()

    data object Loading : AuthorizationState()
}

sealed class AuthorizationEvent {
    data class LoginButtonClicked(
        val login: String,
        val password: String
    ): AuthorizationEvent()
    data object ShowPasswordButtonClicked: AuthorizationEvent()
    data object Clear: AuthorizationEvent()
}

sealed class AuthorizationAction {
    data object NavigateToMatches : AuthorizationAction()
}
