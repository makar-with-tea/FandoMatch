package ru.hse.fandomatch.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.exception.LoginAlreadyInUseException
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.usecase.user.RegisterUseCase
import ru.hse.fandomatch.ui.utils.checkEmailContent
import ru.hse.fandomatch.ui.utils.checkLoginContent
import ru.hse.fandomatch.ui.utils.checkLoginLength
import ru.hse.fandomatch.ui.utils.checkNameContent
import ru.hse.fandomatch.ui.utils.checkNameLength
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class RegistrationViewModel(
    private val registerUseCase: RegisterUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private data class Form(
        var name: String = "",
        var email: String = "",
        var login: String = "",
        var dateOfBirthMillis: Long? = null,
        var gender: Gender? = null,
        var avatarByteArray: ByteArray? = null,
        var password: String = "",
        var passwordRepeat: String = "",
        var agreed: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Form

            if (dateOfBirthMillis != other.dateOfBirthMillis) return false
            if (agreed != other.agreed) return false
            if (name != other.name) return false
            if (email != other.email) return false
            if (login != other.login) return false
            if (gender != other.gender) return false
            if (!avatarByteArray.contentEquals(other.avatarByteArray)) return false
            if (password != other.password) return false
            if (passwordRepeat != other.passwordRepeat) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dateOfBirthMillis?.hashCode() ?: 0
            result = 31 * result + agreed.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + email.hashCode()
            result = 31 * result + login.hashCode()
            result = 31 * result + (gender?.hashCode() ?: 0)
            result = 31 * result + (avatarByteArray?.contentHashCode() ?: 0)
            result = 31 * result + password.hashCode()
            result = 31 * result + passwordRepeat.hashCode()
            return result
        }
    }

    private val form = Form()

    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Name())
    val state: StateFlow<RegistrationState> get() = _state

    private val _action = MutableStateFlow<RegistrationAction?>(null)
    val action: StateFlow<RegistrationAction?> get() = _action

    fun obtainEvent(event: RegistrationEvent) {
        when (event) {
            is RegistrationEvent.NameSubmitted -> handlePersonal(
                name = event.name,
                email = event.email,
                login = event.login
            )
            is RegistrationEvent.DateSelected -> handleDate(event.dateOfBirthMillis)
            is RegistrationEvent.GenderSelected -> handleGender(event.gender)
            is RegistrationEvent.AvatarSelected -> handleAvatar(event.avatarByteArray)
            is RegistrationEvent.PasswordSubmit -> handlePassword(
                password = event.password,
                passwordRepeat = event.passwordRepeat,
                agreed = event.agreedToTerms,
            )
            RegistrationEvent.PasswordVisibilityChanged -> togglePasswordVisibility()
            RegistrationEvent.PasswordRepeatVisibilityChanged -> togglePasswordRepeatVisibility()
            RegistrationEvent.Back -> back()
            RegistrationEvent.Clear -> clear()
        }
    }

    private fun handlePersonal(
        name: String,
        email: String,
        login: String,
    ) {
        var nameErr = RegistrationState.RegistrationError.IDLE
        var emailErr = RegistrationState.RegistrationError.IDLE
        var loginErr = RegistrationState.RegistrationError.IDLE
        var hasError = false

        if (!name.checkNameLength()){
            nameErr = RegistrationState.RegistrationError.NAME_LENGTH
            hasError = true
        }
        if (!name.checkNameContent()) {
            nameErr = RegistrationState.RegistrationError.NAME_CONTENT
            hasError = true
        }
        if (!email.checkEmailContent()) {
            emailErr = RegistrationState.RegistrationError.EMAIL_CONTENT
            hasError = true
        }
        if (!login.checkLoginLength()) {
            loginErr = RegistrationState.RegistrationError.LOGIN_LENGTH
            hasError = true
        } else if (!login.checkLoginContent()) {
            loginErr = RegistrationState.RegistrationError.LOGIN_CONTENT
            hasError = true
        }

        if (hasError) {
            _state.value = RegistrationState.Name(
                name = name,
                email = email,
                login = login,
                nameError = nameErr,
                emailError = emailErr,
                loginError = loginErr
            )
            return
        }

        form.name = name
        form.email = email
        form.login = login
        _state.value = RegistrationState.DateOfBirth(form.dateOfBirthMillis)
    }

    private fun handleDate(dateOfBirthMillis: Long?) {
        if (dateOfBirthMillis == null) {
            _state.value = RegistrationState.DateOfBirth(
                dateOfBirthMillis = null,
                error = RegistrationState.RegistrationError.DOB_EMPTY
            )
            return
        }
        val date = Instant.ofEpochMilli(dateOfBirthMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        if (date.isAfter(LocalDate.now().minusYears(MIN_AGE_IN_YEARS))) {
            _state.value = RegistrationState.DateOfBirth(
                dateOfBirthMillis = dateOfBirthMillis,
                error = RegistrationState.RegistrationError.DOB_TOO_YOUNG
            )
            return
        }
        form.dateOfBirthMillis = dateOfBirthMillis
        _state.value = RegistrationState.GenderChoice(form.gender)
    }

    private fun handleGender(gender: Gender?) {
        if (gender == null) {
            _state.value = RegistrationState.GenderChoice(
                gender = null,
                error = RegistrationState.RegistrationError.GENDER_NOT_SELECTED
            )
            return
        }
        form.gender = gender
        _state.value = RegistrationState.Avatar(form.avatarByteArray)
    }

    private fun handleAvatar(avatarByteArray: ByteArray?) {
        form.avatarByteArray = avatarByteArray
        _state.value = RegistrationState.Password(password = form.password, passwordRepeat = form.passwordRepeat)
    }

    private fun handlePassword(
        password: String,
        passwordRepeat: String,
        agreed: Boolean,
    ) {
        var passErr = RegistrationState.RegistrationError.IDLE
        var repeatErr = RegistrationState.RegistrationError.IDLE
        var hasError = false

        if (password.length < 8) {
            passErr = RegistrationState.RegistrationError.PASSWORD_LENGTH
            hasError = true
        } else {
            val hasLetter = password.any { it.isLetter() }
            val hasDigit = password.any { it.isDigit() }
            if (!hasLetter || !hasDigit) {
                passErr = RegistrationState.RegistrationError.PASSWORD_CONTENT
                hasError = true
            }
        }
        if (password != passwordRepeat) {
            repeatErr = RegistrationState.RegistrationError.PASSWORD_MISMATCH
            hasError = true
        }
        if (hasError || !agreed) {
            _state.value = RegistrationState.Password(
                password = password,
                passwordRepeat = passwordRepeat,
                agreedToTerms = agreed,
                passwordError = passErr,
                passwordRepeatError = repeatErr,
            )
            return
        }

        form.password = password
        form.passwordRepeat = passwordRepeat
        form.agreed = true

        _state.value = RegistrationState.Password(
            password = password,
            passwordRepeat = passwordRepeat,
            agreedToTerms = true,
            isLoading = true
        )

        if (form.dateOfBirthMillis == null || form.gender == null) {
            // This should never happen
            _state.value = RegistrationState.Password(
                password = password,
                passwordRepeat = passwordRepeat,
                agreedToTerms = true,
                isLoading = false
            )
            return
        }

        viewModelScope.launch(dispatcherIO) {
            try {
                registerUseCase.execute(
                    form.name,
                    form.email,
                    form.login,
                    form.dateOfBirthMillis!!,
                    form.gender!!,
                    form.avatarByteArray,
                    form.password
                )
                withContext(dispatcherMain) {
                    _state.value = RegistrationState.Password(
                        password = form.password,
                        passwordRepeat = form.passwordRepeat,
                        agreedToTerms = form.agreed,
                        isLoading = false
                    )
                    _action.value = RegistrationAction.NavigateToMatches
                }
            } catch (e: Exception) {
                withContext(dispatcherMain) {
                    val loginErr = if (e is LoginAlreadyInUseException)
                        RegistrationState.RegistrationError.LOGIN_TAKEN
                    else RegistrationState.RegistrationError.NETWORK
                    _state.value = RegistrationState.Name(
                        name = form.name,
                        email = form.email,
                        login = form.login,
                        loginError = loginErr,
                        nameError = RegistrationState.RegistrationError.NETWORK,
                        emailError = RegistrationState.RegistrationError.NETWORK,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun togglePasswordVisibility() {
        val current = _state.value
        if (current is RegistrationState.Password) {
            _state.value = current.copy(passwordVisibility = !current.passwordVisibility)
        }
    }

    private fun togglePasswordRepeatVisibility() {
        val current = _state.value
        if (current is RegistrationState.Password) {
            _state.value = current.copy(passwordRepeatVisibility = !current.passwordRepeatVisibility)
        }
    }

    private fun back() {
        _state.value = when (_state.value) {
            is RegistrationState.DateOfBirth -> RegistrationState.Name(
                name = form.name,
                email = form.email,
                login = form.login
            )
            is RegistrationState.GenderChoice -> RegistrationState.DateOfBirth(
                dateOfBirthMillis = form.dateOfBirthMillis
            )
            is RegistrationState.Avatar -> RegistrationState.GenderChoice(
                gender = form.gender
            )
            is RegistrationState.Password -> RegistrationState.Avatar(
                avatarByteArray = form.avatarByteArray
            )
            else -> _state.value.also {
                _action.value = RegistrationAction.NavigateBack
            }
        }
    }

    private fun clear() {
        form.apply {
            name = ""
            email = ""
            login = ""
            dateOfBirthMillis = null
            gender = null
            avatarByteArray = null
            password = ""
            passwordRepeat = ""
            agreed = false
        }
        _state.value = RegistrationState.Name()
        _action.value = null
    }
}

private const val MIN_AGE_IN_YEARS : Long = 16