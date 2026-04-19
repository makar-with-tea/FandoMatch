package ru.hse.fandomatch.ui.settings

sealed class SettingsState {
    enum class SettingsError {
        PASSWORD_LENGTH,
        PASSWORD_CONTENT,
        PASSWORD_MISMATCH,
        PASSWORD_INCORRECT,
        ACCOUNT_NOT_FOUND,
        NETWORK_FATAL,
        NETWORK,
        EMAIL_CONTENT,
        INVALID_CODE,
        IDLE
    }
    data object Idle : SettingsState()

    data class Main(
        val email: String = "",
        val matchNotificationsEnabled: Boolean = false,
        val messageNotificationsEnabled: Boolean = false,
        val hideMyPostsFromNonMatches: Boolean = false,
        val isLoading: Boolean = false,
    ) : SettingsState()

    data class ChangePassword(
        val oldPassword: String = "",
        val newPassword: String = "",
        val newPasswordRepeat: String = "",
        val oldPasswordVisibility: Boolean = false,
        val newPasswordVisibility: Boolean = false,
        val newPasswordRepeatVisibility: Boolean = false,
        val email: String = "",
        val matchNotificationsEnabled: Boolean = false,
        val messageNotificationsEnabled: Boolean = false,
        val hideMyPostsFromNonMatches: Boolean = false,
        val oldPasswordError: SettingsError = SettingsError.IDLE,
        val newPasswordError: SettingsError = SettingsError.IDLE,
        val newPasswordRepeatError: SettingsError = SettingsError.IDLE,
        val isLoading: Boolean = false
    ) : SettingsState()

    data object Loading : SettingsState()

    data class Error(
        val error: SettingsError = SettingsError.IDLE,
    ) : SettingsState()

    data object DeletionError: SettingsState()

    data class ChangeEmail(
        val email: String = "",
        val matchNotificationsEnabled: Boolean = false,
        val messageNotificationsEnabled: Boolean = false,
        val hideMyPostsFromNonMatches: Boolean = false,
        val isLoading: Boolean = false,
        val emailError: SettingsError = SettingsError.IDLE,
        val isCode: Boolean = false,
        val codeError: SettingsError = SettingsError.IDLE,
    ) : SettingsState()
}

sealed class SettingsEvent {
    data object LoadProfileData: SettingsEvent()
    data class SavePasswordButtonClicked(
        val newPassword: String,
        val oldPassword: String,
        val newPasswordRepeat: String
    ): SettingsEvent()
    data object SaveEmailButtonClicked: SettingsEvent()
    data class EmailChanged(val email: String): SettingsEvent()
    data class CodeSubmitted(val code: String): SettingsEvent()

    data object ShowOldPasswordButtonClicked : SettingsEvent()
    data object ShowNewPasswordButtonClicked : SettingsEvent()
    data object ShowNewPasswordRepeatButtonClicked : SettingsEvent()
    data object EditPasswordButtonClicked: SettingsEvent()
    data object EditEmailButtonClicked: SettingsEvent()
    data object DeleteAccountButtonClicked: SettingsEvent()
    data object LogoutButtonClicked: SettingsEvent()
    data object MatchNotificationsToggled: SettingsEvent()
    data object MessageNotificationsToggled: SettingsEvent()
    data object HideMyPostsFromNonMatchesToggled: SettingsEvent()
    data object Back: SettingsEvent()
    data object Clear: SettingsEvent()
}

sealed class SettingsAction {
    data object NavigateToIntro : SettingsAction()
}
