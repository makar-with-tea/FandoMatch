package ru.hse.fandomatch.ui.registration

import ru.hse.fandomatch.domain.model.Gender

sealed interface RegistrationState {
    enum class RegistrationError {
        NAME_LENGTH,
        NAME_CONTENT,
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
        GENDER_NOT_SELECTED;
    }

    sealed interface Main: RegistrationState

    data class Name(
        val name: String = "",
        val email: String = "",
        val login: String = "",
        val nameError: RegistrationError = RegistrationError.IDLE,
        val emailError: RegistrationError = RegistrationError.IDLE,
        val loginError: RegistrationError = RegistrationError.IDLE,
        val isLoading: Boolean = false
    ) : Main

    data class DateOfBirth(
        val dateOfBirthMillis: Long? = null,
        val error: RegistrationError = RegistrationError.IDLE
    ) : Main

    data class GenderChoice(
        val gender: Gender? = null,
        val error: RegistrationError = RegistrationError.IDLE
    ) : Main

    data class Avatar(
        val avatarByteArray: ByteArray?,
        val error: RegistrationError = RegistrationError.IDLE,
        val isUploading: Boolean = false
    ) : Main {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Avatar

            if (isUploading != other.isUploading) return false
            if (!avatarByteArray.contentEquals(other.avatarByteArray)) return false
            if (error != other.error) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isUploading.hashCode()
            result = 31 * result + (avatarByteArray?.contentHashCode() ?: 0)
            result = 31 * result + error.hashCode()
            return result
        }
    }

    data class Password(
        val password: String = "",
        val passwordVisibility: Boolean = false,
        val passwordRepeat: String = "",
        val passwordRepeatVisibility: Boolean = false,
        val agreedToTerms: Boolean = false,
        val passwordError: RegistrationError = RegistrationError.IDLE,
        val passwordRepeatError: RegistrationError = RegistrationError.IDLE,
        val isLoading: Boolean = false
    ) : Main

    fun Main.isLoading(): Boolean {
        return when (this) {
            is Name -> isLoading
            is Password -> isLoading
            else -> false
        }
    }

    data object Idle : RegistrationState
    data object Loading : RegistrationState
}

sealed class RegistrationEvent {
    data class NameChanged(val name: String) : RegistrationEvent()
    data class EmailChanged(val email: String) : RegistrationEvent()
    data class LoginChanged(val login: String) : RegistrationEvent()
    data object NameSubmitted : RegistrationEvent()
    data class DateSelected(val dateOfBirthMillis: Long?) : RegistrationEvent()
    data class GenderSelected(val gender: Gender?) : RegistrationEvent()
    data class AvatarSelected(val avatarByteArray: ByteArray?) : RegistrationEvent() { // todo разделить выбор и клик на "далее"
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AvatarSelected

            return avatarByteArray.contentEquals(other.avatarByteArray)
        }

        override fun hashCode(): Int {
            return avatarByteArray?.contentHashCode() ?: 0
        }
    }
    data class PasswordChanged(val password: String) : RegistrationEvent()
    data class PasswordRepeatChanged(val passwordRepeat: String) : RegistrationEvent()
    data class AgreedToTermsChanged(val agreedToTerms: Boolean) : RegistrationEvent()
    data object PasswordSubmit : RegistrationEvent()
    data object PasswordVisibilityChanged : RegistrationEvent()
    data object PasswordRepeatVisibilityChanged : RegistrationEvent()
    data object Back : RegistrationEvent()
    data object Clear : RegistrationEvent()
}

sealed class RegistrationAction {
    data object NavigateToMatches : RegistrationAction()

    data object NavigateBack : RegistrationAction()
}
