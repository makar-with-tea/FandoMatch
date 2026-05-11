package ru.hse.fandomatch.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.exception.EmailAlreadyInUseException
import ru.hse.fandomatch.domain.exception.LoginAlreadyInUseException
import ru.hse.fandomatch.domain.logging.Logger
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.usecase.auth.CheckVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.RegisterUseCase
import ru.hse.fandomatch.domain.usecase.media.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.user.EditProfileUseCase
import ru.hse.fandomatch.utils.checkEmailContent
import ru.hse.fandomatch.utils.checkLoginContent
import ru.hse.fandomatch.utils.checkLoginLength
import ru.hse.fandomatch.utils.checkNameContent
import ru.hse.fandomatch.utils.checkNameLength
import ru.hse.fandomatch.utils.checkPasswordContent
import ru.hse.fandomatch.utils.checkPasswordLength
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class RegistrationViewModel(
    private val registerUseCase: RegisterUseCase,
    private val getVerificationCodeUseCase: GetVerificationCodeUseCase,
    private val checkVerificationCodeUseCase: CheckVerificationCodeUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val editProfileUseCase: EditProfileUseCase,
    private val logger: Logger,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private data class Form(
        var name: String = "",
        var email: String = "",
        var login: String = "",
        var dateOfBirthEpochSeconds: Long? = null,
        var gender: Gender = Gender.NOT_SPECIFIED,
        var avatarByteArray: ByteArray? = null,
        var password: String = "",
        var passwordRepeat: String = "",
        var agreed: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Form

            if (dateOfBirthEpochSeconds != other.dateOfBirthEpochSeconds) return false
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
            var result = dateOfBirthEpochSeconds?.hashCode() ?: 0
            result = 31 * result + agreed.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + email.hashCode()
            result = 31 * result + login.hashCode()
            result = 31 * result + (gender.hashCode())
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
            is RegistrationEvent.NameChanged -> onNameChanged(event.name)
            is RegistrationEvent.EmailChanged -> onEmailChanged(event.email)
            is RegistrationEvent.LoginChanged -> onLoginChanged(event.login)
            RegistrationEvent.NameSubmitted -> handlePersonal()
            is RegistrationEvent.CodeSubmitted -> handleCode(event.code)
            is RegistrationEvent.DateSelected -> handleDate(event.dateOfBirthEpochSeconds)
            is RegistrationEvent.GenderSelected -> handleGender(event.gender)
            is RegistrationEvent.AvatarSelected -> handleAvatar(event.avatarByteArray)
            is RegistrationEvent.PasswordChanged -> onPasswordChanged(event.password)
            is RegistrationEvent.PasswordRepeatChanged -> onPasswordRepeatChanged(event.passwordRepeat)
            is RegistrationEvent.AgreedToTermsChanged -> onAgreedToTermsChanged(event.agreedToTerms)
            RegistrationEvent.PasswordSubmit -> handlePassword()
            RegistrationEvent.PasswordVisibilityChanged -> togglePasswordVisibility()
            RegistrationEvent.PasswordRepeatVisibilityChanged -> togglePasswordRepeatVisibility()
            RegistrationEvent.Back -> back()
            RegistrationEvent.Clear -> clear()
        }
    }

    private fun onNameChanged(name: String) {
        val currentState = _state.value as? RegistrationState.Name ?: return
        val nameErr = when {
            !name.checkNameLength() -> RegistrationState.RegistrationError.NAME_LENGTH
            !name.checkNameContent() -> RegistrationState.RegistrationError.NAME_CONTENT
            else -> RegistrationState.RegistrationError.IDLE
        }
        _state.value = currentState.copy(
            name = name,
            nameError = nameErr
        )
    }

    private fun onEmailChanged(email: String) {
        val currentState = _state.value as? RegistrationState.Name ?: return
        val emailErr = if (!email.checkEmailContent())
            RegistrationState.RegistrationError.EMAIL_CONTENT
        else RegistrationState.RegistrationError.IDLE
        _state.value = currentState.copy(
            email = email,
            emailError = emailErr
        )
    }

    private fun onLoginChanged(login: String) {
        val currentState = _state.value as? RegistrationState.Name ?: return
        val loginErr = when {
            !login.checkLoginLength() -> RegistrationState.RegistrationError.LOGIN_LENGTH
            !login.checkLoginContent() -> RegistrationState.RegistrationError.LOGIN_CONTENT
            else -> RegistrationState.RegistrationError.IDLE
        }
        _state.value = currentState.copy(
            login = login,
            loginError = loginErr
        )
    }

    private fun handlePersonal() {
        val currentState = _state.value as? RegistrationState.Name ?: return
        var nameErr = RegistrationState.RegistrationError.IDLE
        var emailErr = RegistrationState.RegistrationError.IDLE
        var loginErr = RegistrationState.RegistrationError.IDLE
        var hasError = false

        if (!currentState.name.checkNameLength()) {
            nameErr = RegistrationState.RegistrationError.NAME_LENGTH
            hasError = true
        }
        if (!currentState.email.checkEmailContent()) {
            emailErr = RegistrationState.RegistrationError.EMAIL_CONTENT
            hasError = true
        }
        if (!currentState.login.checkLoginLength()) {
            loginErr = RegistrationState.RegistrationError.LOGIN_LENGTH
            hasError = true
        }

        if (hasError) {
            _state.value = currentState.copy(
                nameError = nameErr,
                emailError = emailErr,
                loginError = loginErr
            )
            return
        }
        _state.value = currentState.copy(
            isLoading = true
        )

        viewModelScope.launch(dispatcherIO) {
            getVerificationCodeUseCase.execute(currentState.email)
                .onFailure { e ->
                    logger.e("RegistrationViewModel", "Failed to get verification code", e)
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            nameError = RegistrationState.RegistrationError.NETWORK,
                            emailError = RegistrationState.RegistrationError.NETWORK,
                            loginError = RegistrationState.RegistrationError.NETWORK
                        )
                    }
                    return@launch
                }
                .onSuccess {
                    withContext(dispatcherMain) {
                        form.name = currentState.name
                        form.email = currentState.email
                        form.login = currentState.login
                        _state.value = RegistrationState.Code()
                    }
                }
        }
    }

    private fun handleCode(code: String) {
        _state.value = (state.value as? RegistrationState.Code)?.copy(isLoading = true) ?: return

        viewModelScope.launch(dispatcherIO) {
            checkVerificationCodeUseCase.execute(code, form.email)
                .onFailure {
                    logger.e("RegistrationViewModel", "Error while checking verification code", it)
                    withContext(dispatcherMain) {
                        _state.value = RegistrationState.Code(
                            codeError = RegistrationState.RegistrationError.NETWORK,
                            isLoading = false
                        )
                    }
                }
                .onSuccess { isValid ->
                    logger.d("RegistrationViewModel", "Verification code check result: $isValid")
                    withContext(dispatcherMain) {
                        if (isValid) {
                            _state.value = RegistrationState.DateOfBirth(form.dateOfBirthEpochSeconds)
                        } else {
                            _state.value = RegistrationState.Code(
                                codeError = RegistrationState.RegistrationError.INVALID_CODE,
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    private fun handleDate(dateOfBirthEpochSeconds: Long?) {
        if (dateOfBirthEpochSeconds == null) {
            _state.value = RegistrationState.DateOfBirth(
                dateOfBirthEpochSeconds = null,
                error = RegistrationState.RegistrationError.DOB_EMPTY
            )
            return
        }
        val date = Instant.ofEpochSecond(dateOfBirthEpochSeconds).atZone(ZoneId.systemDefault()).toLocalDate()
        if (date.isAfter(LocalDate.now().minusYears(MIN_AGE_IN_YEARS))) {
            _state.value = RegistrationState.DateOfBirth(
                dateOfBirthEpochSeconds = dateOfBirthEpochSeconds,
                error = RegistrationState.RegistrationError.DOB_TOO_YOUNG
            )
            return
        }
        form.dateOfBirthEpochSeconds = dateOfBirthEpochSeconds
        _state.value = RegistrationState.GenderChoice(form.gender)
    }

    private fun handleGender(gender: Gender) {
        form.gender = gender
        _state.value = RegistrationState.Avatar(form.avatarByteArray)
    }

    private fun handleAvatar(avatarByteArray: ByteArray?) {
        form.avatarByteArray = avatarByteArray
        _state.value = RegistrationState.Password(password = form.password, passwordRepeat = form.passwordRepeat)
    }

    private fun onPasswordChanged(password: String) {
        val currentState = _state.value as? RegistrationState.Password ?: return
        val passErr = when {
            !password.checkPasswordLength() -> RegistrationState.RegistrationError.PASSWORD_LENGTH
            !password.checkPasswordContent() -> RegistrationState.RegistrationError.PASSWORD_CONTENT
            else -> RegistrationState.RegistrationError.IDLE
        }
        _state.value = currentState.copy(
            password = password,
            passwordError = passErr
        )
    }

    private fun onPasswordRepeatChanged(passwordRepeat: String) {
        val currentState = _state.value as? RegistrationState.Password ?: return
        val repeatErr = if (passwordRepeat != currentState.password)
            RegistrationState.RegistrationError.PASSWORD_MISMATCH
        else RegistrationState.RegistrationError.IDLE
        _state.value = currentState.copy(
            passwordRepeat = passwordRepeat,
            passwordRepeatError = repeatErr
        )
    }

    private fun onAgreedToTermsChanged(agreedToTerms: Boolean) {
        val currentState = _state.value as? RegistrationState.Password ?: return
        _state.value = currentState.copy(
            agreedToTerms = agreedToTerms
        )
    }

    private fun handlePassword() {
        val currentState = _state.value as? RegistrationState.Password ?: return
        var passErr = RegistrationState.RegistrationError.IDLE
        var repeatErr = RegistrationState.RegistrationError.IDLE
        var hasError = false

        if (!currentState.password.checkPasswordLength()) {
            passErr = RegistrationState.RegistrationError.PASSWORD_LENGTH
            hasError = true
        } else {
            if (!currentState.password.checkPasswordContent()) {
                passErr = RegistrationState.RegistrationError.PASSWORD_CONTENT
                hasError = true
            }
        }
        if (currentState.password != currentState.passwordRepeat) {
            repeatErr = RegistrationState.RegistrationError.PASSWORD_MISMATCH
            hasError = true
        }
        if (hasError || !currentState.agreedToTerms) {
            _state.value = currentState.copy(
                passwordError = passErr,
                passwordRepeatError = repeatErr,
            )
            return
        }

        form.password = currentState.password
        form.passwordRepeat = currentState.passwordRepeat
        form.agreed = true

        _state.value = RegistrationState.Password(
            password = currentState.password,
            passwordRepeat = currentState.passwordRepeat,
            agreedToTerms = true,
            isLoading = true
        )

        if (form.dateOfBirthEpochSeconds == null) {
            // This should never happen
            _state.value = RegistrationState.Password(
                password = currentState.password,
                passwordRepeat = currentState.passwordRepeat,
                agreedToTerms = true,
                isLoading = false
            )
            return
        }

        viewModelScope.launch(dispatcherIO) {
            registerUseCase.execute(
                form.name,
                form.email,
                form.login,
                form.dateOfBirthEpochSeconds!!,
                form.gender,
                form.password
            )
                .onFailure { e ->
                    logger.e("RegistrationViewModel", "Registration failed", e)
                    val loginErr = when (e) {
                        is LoginAlreadyInUseException -> RegistrationState.RegistrationError.LOGIN_TAKEN
                        is EmailAlreadyInUseException -> RegistrationState.RegistrationError.IDLE
                        else -> RegistrationState.RegistrationError.NETWORK
                    }
                    val emailErr = when (e) {
                        is EmailAlreadyInUseException -> RegistrationState.RegistrationError.EMAIL_TAKEN
                        is LoginAlreadyInUseException -> RegistrationState.RegistrationError.IDLE
                        else -> RegistrationState.RegistrationError.NETWORK
                    }
                    val nameErr = if (e is LoginAlreadyInUseException || e is EmailAlreadyInUseException)
                        RegistrationState.RegistrationError.IDLE
                    else RegistrationState.RegistrationError.NETWORK
                    withContext(dispatcherMain) {
                        _state.value = RegistrationState.Name(
                            name = form.name,
                            email = form.email,
                            login = form.login,
                            loginError = loginErr,
                            nameError = nameErr,
                            emailError = emailErr,
                            isLoading = false
                        )
                    }
                }
                .onSuccess {
                    form.avatarByteArray?.let {
                        uploadMediaUseCase.execute(
                            it,
                            MediaType.IMAGE
                        )
                            .onFailure { e ->
                                logger.e("RegistrationViewModel", "Failed to upload avatar", e)
                            }
                            .onSuccess { avatarId ->
                                logger.d("RegistrationViewModel", "Avatar uploaded successfully with id $avatarId")
                                editProfileUseCase.execute(
                                    name = form.name,
                                    bio = null,
                                    city = null,
                                    fandoms = listOf(),
                                    avatarMediaId = avatarId,
                                    backgroundMediaId = null
                                )
                                    .onFailure { e1 ->
                                        logger.e("RegistrationViewModel", "Failed to set avatar for user profile", e1)
                                    }
                            }
                    }
                    withContext(dispatcherMain) {
                        _state.value = RegistrationState.Password(
                            password = form.password,
                            passwordRepeat = form.passwordRepeat,
                            agreedToTerms = form.agreed,
                            isLoading = false
                        )
                        _action.value = RegistrationAction.NavigateToMatches
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
            is RegistrationState.Code -> RegistrationState.Name(
                name = form.name,
                email = form.email,
                login = form.login
            )
            is RegistrationState.DateOfBirth -> RegistrationState.Code()
            is RegistrationState.GenderChoice -> RegistrationState.DateOfBirth(
                dateOfBirthEpochSeconds = form.dateOfBirthEpochSeconds
            )
            is RegistrationState.Avatar -> RegistrationState.GenderChoice(
                gender = form.gender
            )
            is RegistrationState.Password -> RegistrationState.Avatar(
                avatarByteArray = form.avatarByteArray
            )
            is RegistrationState.Idle, is RegistrationState.Loading, is RegistrationState.Name
                -> _state.value.also {
                _action.value = RegistrationAction.NavigateBack
            }
        }
    }

    private fun clear() {
        form.apply {
            name = ""
            email = ""
            login = ""
            dateOfBirthEpochSeconds = null
            gender = Gender.NOT_SPECIFIED
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