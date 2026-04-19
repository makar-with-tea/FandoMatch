package ru.hse.fandomatch.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.utils.checkEmailContent
import ru.hse.fandomatch.utils.checkPasswordContent
import ru.hse.fandomatch.utils.checkPasswordLength
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.usecase.auth.ChangeEmailUseCase
import ru.hse.fandomatch.domain.usecase.auth.ChangePasswordUseCase
import ru.hse.fandomatch.domain.usecase.auth.CheckVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.DeleteAccountUseCase
import ru.hse.fandomatch.domain.usecase.auth.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.LogoutUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserPreferencesUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase
import ru.hse.fandomatch.domain.usecase.user.UpdateUserPreferencesUseCase

class SettingsViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val getVerificationCodeUseCase: GetVerificationCodeUseCase,
    private val checkVerificationCodeUseCase: CheckVerificationCodeUseCase,
    private val changeEmailUseCase: ChangeEmailUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState.Idle)
    val state : MutableStateFlow<SettingsState>
        get() = _state
    private val _action = MutableStateFlow<SettingsAction?>(null)
    val action : MutableStateFlow<SettingsAction?>
        get() = _action

    fun obtainEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadProfileData -> {
                loadProfileData()
            }
            SettingsEvent.Clear -> clear()
            SettingsEvent.Back -> handleBack()
            SettingsEvent.DeleteAccountButtonClicked -> deleteAccount()
            SettingsEvent.EditEmailButtonClicked -> editEmail()
            SettingsEvent.EditPasswordButtonClicked -> editPassword()
            SettingsEvent.LogoutButtonClicked -> logout()
            SettingsEvent.SaveEmailButtonClicked -> saveEmail()
            is SettingsEvent.EmailChanged -> updateEmail(event.email)
            is SettingsEvent.CodeSubmitted -> checkCode(event.code)
            is SettingsEvent.SavePasswordButtonClicked -> savePassword(
                newPassword = event.newPassword,
                oldPassword = event.oldPassword,
                newPasswordRepeat = event.newPasswordRepeat
            )
            SettingsEvent.ShowNewPasswordButtonClicked -> showNewPassword()
            SettingsEvent.ShowNewPasswordRepeatButtonClicked -> showNewPasswordRepeat()
            SettingsEvent.ShowOldPasswordButtonClicked -> showOldPassword()
            SettingsEvent.MatchNotificationsToggled -> toggleMatchNotifications()
            SettingsEvent.MessageNotificationsToggled -> toggleMessageNotifications()
            SettingsEvent.HideMyPostsFromNonMatchesToggled -> toggleHideMyPostsFromNonMatches()
        }
    }

    private fun loadProfileData() {
        _state.value = SettingsState.Loading
        viewModelScope.launch(dispatcherIO) {
            val result = getUserUseCase.execute(null, true)
            val userInfo = result.getOrNull() ?: run {
                withContext(dispatcherMain) {
                    _state.value = SettingsState.Error(
                        error = SettingsState.SettingsError.NETWORK_FATAL
                    )
                }
                return@launch
            }
            val preferencesResult = getUserPreferencesUseCase.execute()
            val userPreferences = preferencesResult.getOrNull() ?: run {
                withContext(dispatcherMain) {
                    _state.value = SettingsState.Error(
                        error = SettingsState.SettingsError.NETWORK_FATAL
                    )
                }
                return@launch
            }
            withContext(dispatcherMain) {
                _state.value = SettingsState.Main(
                    email = (userInfo.profileType as ProfileType.Own).email,
                    matchNotificationsEnabled = userPreferences.matchesEnabled,
                    messageNotificationsEnabled = userPreferences.messagesEnabled,
                    hideMyPostsFromNonMatches = userPreferences.hideMyPostsFromNonMatches
                )
            }
        }
    }

    private fun toggleMatchNotifications() {
        val currentState = _state.value as? SettingsState.Main ?: return
        val newValue = !currentState.matchNotificationsEnabled
        _state.value = currentState.copy(
            matchNotificationsEnabled = newValue
        )
        viewModelScope.launch(dispatcherIO) {
            val result = updateUserPreferencesUseCase.execute(
                matchNotificationsEnabled = newValue,
                messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches
            )
            if (result.isFailure) {
                Log.e("SettingsViewModel", "Failed to update user preferences", result.exceptionOrNull())
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        matchNotificationsEnabled = !newValue
                    )
                }
            }
        }
    }

    private fun toggleMessageNotifications() {
        val currentState = _state.value as? SettingsState.Main ?: return
        val newValue = !currentState.messageNotificationsEnabled
        _state.value = currentState.copy(
            messageNotificationsEnabled = newValue
        )
        viewModelScope.launch(dispatcherIO) {
            val result = updateUserPreferencesUseCase.execute(
                matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                messageNotificationsEnabled = newValue,
                hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches
            )
            if (result.isFailure) {
                Log.e("SettingsViewModel", "Failed to update user preferences", result.exceptionOrNull())
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        messageNotificationsEnabled = !newValue
                    )
                }
            }
        }
    }

    private fun toggleHideMyPostsFromNonMatches() {
        val currentState = _state.value as? SettingsState.Main ?: return
        val newValue = !currentState.hideMyPostsFromNonMatches
        _state.value = currentState.copy(
            hideMyPostsFromNonMatches = newValue
        )
        viewModelScope.launch(dispatcherIO) {
            val result = updateUserPreferencesUseCase.execute(
                matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                hideMyPostsFromNonMatches = newValue
            )
            if (result.isFailure) {
                Log.e("SettingsViewModel", "Failed to update user preferences", result.exceptionOrNull())
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        hideMyPostsFromNonMatches = !newValue
                    )
                }
            }
        }
    }

    private fun savePassword(newPassword: String, oldPassword: String, newPasswordRepeat: String) {
        var currentState = _state.value as? SettingsState.ChangePassword ?: return
        currentState = currentState.copy(
            oldPasswordError = SettingsState.SettingsError.IDLE,
            newPasswordError = SettingsState.SettingsError.IDLE,
            newPasswordRepeatError = SettingsState.SettingsError.IDLE
        )

        if (!newPassword.checkPasswordLength()) {
            _state.value = currentState.copy(
                newPasswordError = SettingsState.SettingsError.PASSWORD_LENGTH
            )
            return
        }

        if (!newPassword.checkPasswordContent()) {
            _state.value = currentState.copy(
                newPasswordError = SettingsState.SettingsError.PASSWORD_CONTENT
            )
            return
        }

        if (newPassword != newPasswordRepeat) {
            _state.value = currentState.copy(
                newPasswordRepeatError = SettingsState.SettingsError.PASSWORD_MISMATCH
            )
            return
        }

        _state.value = currentState.copy(
            isLoading = true
        )
        viewModelScope.launch(dispatcherIO) {
            val result = changePasswordUseCase.execute(oldPassword, newPassword)
            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                Log.e("SettingsViewModel", "Failed to change password", exception)
                // todo correct exception
                if (exception is InvalidCredentialsException) {
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            isLoading = false,
                            oldPasswordError = SettingsState.SettingsError.PASSWORD_INCORRECT,
                        )
                    }
                } else {
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            isLoading = false,
                            oldPasswordError = SettingsState.SettingsError.NETWORK,
                            newPasswordError = SettingsState.SettingsError.NETWORK,
                            newPasswordRepeatError = SettingsState.SettingsError.NETWORK
                        )
                    }
                }
                return@launch
            }

            withContext(dispatcherMain) {
                _state.value = SettingsState.Main(
                    email = currentState.email,
                    matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                    messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                    hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
                )
            }
        }
    }

    private fun updateEmail(email: String) {
        val currentState = _state.value as? SettingsState.ChangeEmail ?: return
        if (currentState.isCode) return
        val error = when {
            !email.checkEmailContent() -> SettingsState.SettingsError.EMAIL_CONTENT
            else -> SettingsState.SettingsError.IDLE
        }
        _state.value = currentState.copy(
            email = email,
            emailError = error
        )
    }

    private fun saveEmail() {
        var currentState = _state.value as? SettingsState.ChangeEmail ?: return
        val email = currentState.email
        currentState = currentState.copy(
            emailError = SettingsState.SettingsError.IDLE,
            isLoading = false
        )

        if (!email.checkEmailContent()) {
            _state.value = currentState.copy(
                emailError = SettingsState.SettingsError.EMAIL_CONTENT
            )
            return
        }

        _state.value = currentState.copy(isLoading = true)
        viewModelScope.launch(dispatcherIO) {
            val result = getVerificationCodeUseCase.execute(email)
            if (result.isFailure) {
                Log.e("SettingsViewModel", "Failed to get verification code", result.exceptionOrNull())
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        emailError = SettingsState.SettingsError.NETWORK,
                        isLoading = false
                    )
                }
                return@launch
            }
            _state.value = currentState.copy(
                isCode = true,
                emailError = SettingsState.SettingsError.IDLE,
                codeError = SettingsState.SettingsError.IDLE,
                isLoading = false
            )
        }
    }

    private fun checkCode(code: String) {
        val currentState = _state.value as? SettingsState.ChangeEmail ?: return
        if (!currentState.isCode) return
        _state.value = currentState.copy(
            isLoading = true
        )

        viewModelScope.launch(dispatcherIO) {
            val result = checkVerificationCodeUseCase.execute(code, currentState.email)
            val isValid = result.getOrNull() ?: run {
                withContext(dispatcherMain) {
                    _state.value = SettingsState.ChangeEmail(
                        codeError = SettingsState.SettingsError.NETWORK,
                        isLoading = false,
                        matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                        messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                        hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
                    )
                }
                return@launch
            }
            if (!isValid) {
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        codeError = SettingsState.SettingsError.INVALID_CODE,
                        isLoading = false
                    )
                }
                return@launch
            }
            val changeEmailResult = changeEmailUseCase.execute(currentState.email)
            if (changeEmailResult.isFailure) {
                Log.e(
                    "SettingsViewModel",
                    "Failed to change email",
                    changeEmailResult.exceptionOrNull()
                )
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        codeError = SettingsState.SettingsError.NETWORK,
                        isLoading = false,
                    )
                }
                return@launch
            }
            withContext(dispatcherMain) {
                _state.value = SettingsState.Main(
                    email = currentState.email,
                    matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                    messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                    hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
                )
            }
        }
    }

    private fun showOldPassword() {
        if (_state.value is SettingsState.ChangePassword) {
            _state.value = (_state.value as SettingsState.ChangePassword).copy(
                oldPasswordVisibility =
                    !(_state.value as SettingsState.ChangePassword).oldPasswordVisibility)
        }
    }

    private fun showNewPassword() {
        if (_state.value is SettingsState.ChangePassword) {
            _state.value = (_state.value as SettingsState.ChangePassword).copy(
                newPasswordVisibility =
                    !(_state.value as SettingsState.ChangePassword).newPasswordVisibility)
        }
    }

    private fun showNewPasswordRepeat() {
        if (_state.value is SettingsState.ChangePassword) {
            _state.value = (_state.value as SettingsState.ChangePassword).copy(
                newPasswordRepeatVisibility =
                    !(_state.value as SettingsState.ChangePassword).newPasswordRepeatVisibility)
        }
    }

    private fun editPassword() {
        val currentState = _state.value as? SettingsState.Main ?: return
        _state.value = SettingsState.ChangePassword(
            email = currentState.email,
            matchNotificationsEnabled = currentState.matchNotificationsEnabled,
            messageNotificationsEnabled = currentState.messageNotificationsEnabled,
            hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
        )
    }

    private fun editEmail() {
        val currentState = _state.value as? SettingsState.Main ?: return
        _state.value = SettingsState.ChangeEmail(
            email = currentState.email,
            matchNotificationsEnabled = currentState.matchNotificationsEnabled,
            messageNotificationsEnabled = currentState.messageNotificationsEnabled,
            hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
        )
    }

    private fun deleteAccount() {
        if (_state.value !is SettingsState.Main) return
        _state.value = (_state.value as SettingsState.Main).copy(
            isLoading = true
        )
        viewModelScope.launch(dispatcherIO) {
            val result = deleteAccountUseCase.execute()
            if (result.isFailure) {
                Log.e("SettingsViewModel", "Failed to delete account", result.exceptionOrNull())
                withContext(dispatcherMain) {
                    _state.value = SettingsState.DeletionError
                }
                return@launch
            }
            withContext(dispatcherMain) {
                _action.value = SettingsAction.NavigateToIntro
            }
        }
    }

    private fun handleBack() {
        _state.value = when (
            val currentState = _state.value
        ) {
            is SettingsState.ChangeEmail -> if (currentState.isCode) {
                currentState.copy(
                    isCode = false,
                    codeError = SettingsState.SettingsError.IDLE,
                    emailError = SettingsState.SettingsError.IDLE,
                    isLoading = false,
                )
            } else {
                SettingsState.Main(
                    email = currentState.email,
                    matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                    messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                    hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
                )
            }
            is SettingsState.ChangePassword -> SettingsState.Main(
                email = currentState.email,
                matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
            )
            is SettingsState.Main,
            is SettingsState.DeletionError,
            is SettingsState.Error,
            SettingsState.Idle,
            SettingsState.Loading
                -> currentState
        }
    }

    private fun logout() {
        _state.value = SettingsState.Loading
        viewModelScope.launch(dispatcherIO) {
            logoutUseCase.execute()
            withContext(dispatcherMain) {
                _action.value = SettingsAction.NavigateToIntro
            }
        }
    }

    private fun clear() {
        _state.value = SettingsState.Idle
        _action.value = null
    }
}
