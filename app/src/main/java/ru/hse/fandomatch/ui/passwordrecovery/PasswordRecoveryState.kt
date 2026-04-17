package ru.hse.fandomatch.ui.passwordrecovery

sealed class PasswordRecoveryState {
    enum class PasswordRecoveryError {
        EMAIL_CONTENT,
        EMPTY_CODE,
        INVALID_CODE,
        PASSWORD_LENGTH,
        PASSWORD_CONTENT,
        PASSWORD_MISMATCH,
        NETWORK,
        IDLE;

        fun isButtonAvailable(): Boolean {
            return this == IDLE || this == NETWORK || this == INVALID_CODE
        }
    }

    data class Email(
        val email: String = "",
        val emailError: PasswordRecoveryError = PasswordRecoveryError.IDLE,
    ) : PasswordRecoveryState()

    data class Main(
        val newPassword: String = "",
        val repeatNewPassword: String = "",
        val newPasswordVisibility: Boolean = false,
        val repeatNewPasswordVisibility: Boolean = false,
        val isLoading: Boolean = false,
        val codeError: PasswordRecoveryError = PasswordRecoveryError.IDLE,
        val newPasswordError: PasswordRecoveryError = PasswordRecoveryError.IDLE,
        val repeatNewPasswordError: PasswordRecoveryError = PasswordRecoveryError.IDLE,
    ) : PasswordRecoveryState()
}

sealed class PasswordRecoveryEvent {
    data class EmailChanged(val email: String) : PasswordRecoveryEvent()
    data object SendCodeClicked : PasswordRecoveryEvent()
    data class NewPasswordChanged(val password: String) : PasswordRecoveryEvent()
    data class RepeatNewPasswordChanged(val password: String) : PasswordRecoveryEvent()
    data object ToggleNewPasswordVisibility : PasswordRecoveryEvent()
    data object ToggleRepeatNewPasswordVisibility : PasswordRecoveryEvent()
    data class SavePasswordClicked(val code: String) : PasswordRecoveryEvent()
    data object Clear : PasswordRecoveryEvent()
}

sealed class PasswordRecoveryAction {
    data object NavigateToAuthorization : PasswordRecoveryAction()
}
