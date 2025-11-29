package ru.hse.fandomatch.ui.registration

sealed class RegistrationState {
    enum class RegistrationError {
        NAME_LENGTH,
        NAME_CONTENT,
        SURNAME_LENGTH,
        SURNAME_CONTENT,
        LOGIN_LENGTH,
        LOGIN_CONTENT,
        PASSWORD_LENGTH,
        PASSWORD_CONTENT,
        PASSWORD_MISMATCH,
        EMAIL_CONTENT,
        LOGIN_TAKEN,
        NETWORK,
        IDLE,
        DOB_TOO_YOUNG,
        DOB_EMPTY,
        GENDER_NOT_SELECTED,
    }

    data class Name(
        val name: String = "",
        val email: String = "",
        val login: String = "",
        val nameError: RegistrationError = RegistrationError.IDLE,
        val emailError: RegistrationError = RegistrationError.IDLE,
        val loginError: RegistrationError = RegistrationError.IDLE,
        val isLoading: Boolean = false
    ) : RegistrationState()

    data class DateOfBirth(
        val dateOfBirthMillis: Long? = null,
        val error: RegistrationError = RegistrationError.IDLE
    ) : RegistrationState()

    data class Gender(
        val gender: GenderType? = null,
        val error: RegistrationError = RegistrationError.IDLE
    ) : RegistrationState()

    data class Avatar(
        val avatarUri: String? = null,
        val error: RegistrationError = RegistrationError.IDLE,
        val isUploading: Boolean = false
    ) : RegistrationState()

    data class Password(
        val password: String = "",
        val passwordVisibility: Boolean = false,
        val passwordRepeat: String = "",
        val passwordRepeatVisibility: Boolean = false,
        val agreedToTerms: Boolean = false,
        val passwordError: RegistrationError = RegistrationError.IDLE,
        val passwordRepeatError: RegistrationError = RegistrationError.IDLE,
        val isLoading: Boolean = false
    ) : RegistrationState()

    data object Idle : RegistrationState()
    data object Loading : RegistrationState()
}

sealed class RegistrationEvent {
    data class NameSubmitted(
        val name: String,
        val email: String,
        val login: String
    ) : RegistrationEvent()

    data class DateSelected(val dateOfBirthMillis: Long?) : RegistrationEvent()
    data class GenderSelected(val gender: GenderType?) : RegistrationEvent()
    data class AvatarSelected(val avatarUri: String?) : RegistrationEvent()
    data class PasswordSubmit(
        val password: String,
        val passwordRepeat: String,
        val agreedToTerms: Boolean,
    ) : RegistrationEvent()

    data object PasswordVisibilityChanged : RegistrationEvent()
    data object PasswordRepeatVisibilityChanged : RegistrationEvent()
    data object Back : RegistrationEvent()
    data object Clear : RegistrationEvent()
}

sealed class RegistrationAction {
    data object NavigateToMatches : RegistrationAction()

    data object NavigateBack : RegistrationAction()
}

enum class GenderType { MALE, FEMALE, UNSPECIFIED }