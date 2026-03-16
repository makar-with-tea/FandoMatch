package ru.hse.fandomatch.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.data.mock.mockUserPreferences
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.checkEmailContent
import ru.hse.fandomatch.checkPasswordContent
import ru.hse.fandomatch.checkPasswordLength

class SettingsViewModel(
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
            is SettingsEvent.SaveEmailButtonClicked -> saveEmail(event.email)
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
            try {
                // todo load user info
                val userInfo = mockUser
//                    ?: run {
//                    withContext(dispatcherMain) {
//                        _state.value = SettingsState.Error(
//                            error = SettingsState.SettingsError.ACCOUNT_NOT_FOUND
//                        )
//                    }
//                    return@launch
//                }
                // todo load notification preferences
                val userPreferences = mockUserPreferences
                withContext(dispatcherMain) {
                    _state.value = SettingsState.Main(
                        email = userInfo.email,
                        matchNotificationsEnabled = userPreferences.matchesEnabled,
                        messageNotificationsEnabled = userPreferences.messagesEnabled,
                        hideMyPostsFromNonMatches = userPreferences.hideMyPostsFromNonMatches
                    )
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = SettingsState.Error(
                        error = SettingsState.SettingsError.NETWORK_FATAL
                    )
                }
            }
        }
    }

    private fun toggleMatchNotifications() {
        if (_state.value !is SettingsState.Main) return
        val currentState = _state.value as SettingsState.Main
        val newValue = !currentState.matchNotificationsEnabled
        _state.value = currentState.copy(
            matchNotificationsEnabled = newValue
        )
        viewModelScope.launch(dispatcherIO) {
            try {
                // todo save new value to server
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        matchNotificationsEnabled = !newValue
                    )
                }
            }
        }
    }

    private fun toggleMessageNotifications() {
        if (_state.value !is SettingsState.Main) return
        val currentState = _state.value as SettingsState.Main
        val newValue = !currentState.messageNotificationsEnabled
        _state.value = currentState.copy(
            messageNotificationsEnabled = newValue
        )
        viewModelScope.launch(dispatcherIO) {
            try {
                // todo save new value to server
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        messageNotificationsEnabled = !newValue
                    )
                }
            }
        }
    }

    private fun toggleHideMyPostsFromNonMatches() {
        if (_state.value !is SettingsState.Main) return
        val currentState = _state.value as SettingsState.Main
        val newValue = !currentState.hideMyPostsFromNonMatches
        _state.value = currentState.copy(
            hideMyPostsFromNonMatches = newValue
        )
        viewModelScope.launch(dispatcherIO) {
            try {
                // todo save new value to server
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        hideMyPostsFromNonMatches = !newValue
                    )
                }
            }
        }
    }

    private fun savePassword(newPassword: String, oldPassword: String, newPasswordRepeat: String) {
        if (_state.value !is SettingsState.ChangePassword) return
        var currentState = _state.value as SettingsState.ChangePassword
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
            try {
                // todo check old password
                val isOldPasswordCorrect = true
                if (!isOldPasswordCorrect) {
                    _state.value = currentState.copy(
                        oldPasswordError = SettingsState.SettingsError.PASSWORD_INCORRECT,
                        isLoading = false
                    )
                    return@launch
                }
                // todo change password
                withContext(dispatcherMain) {
                    _state.value = SettingsState.Main(
                        email = currentState.email,
                        matchNotificationsEnabled = currentState.matchNotificationsEnabled,
                        messageNotificationsEnabled = currentState.messageNotificationsEnabled,
                        hideMyPostsFromNonMatches = currentState.hideMyPostsFromNonMatches,
                    )
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        isLoading = false,
                        oldPasswordError = SettingsState.SettingsError.NETWORK,
                        newPasswordError = SettingsState.SettingsError.NETWORK,
                        newPasswordRepeatError = SettingsState.SettingsError.NETWORK
                    )
                }
            }
        }
    }

    private fun saveEmail(email: String) {
        if (_state.value !is SettingsState.ChangeEmail) return
        var currentState = _state.value as SettingsState.ChangeEmail
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
            try {
                // todo change email
                withContext(dispatcherMain) {
                    _state.value = SettingsState.Main(
                        email = email,
                    )
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(
                        emailError = SettingsState.SettingsError.NETWORK,
                        isLoading = false
                    )
                }
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
        if (_state.value !is SettingsState.Main) return
        _state.value = SettingsState.ChangePassword(
            email = (_state.value as SettingsState.Main).email,
        )
    }

    private fun editEmail() {
        if (_state.value !is SettingsState.Main) return
        _state.value = SettingsState.ChangeEmail(
            email = (_state.value as SettingsState.Main).email,
        )
    }

    private fun deleteAccount() {
        if (_state.value !is SettingsState.Main) return
        _state.value = (_state.value as SettingsState.Main).copy(
            isLoading = true
        )
        viewModelScope.launch(dispatcherIO) {
            try {
                // todo delete current account
                withContext(dispatcherMain) {
                    _action.value = SettingsAction.NavigateToIntro
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = SettingsState.DeletionError
                }
            }
        }
    }

    private fun handleBack() {
        _state.value = when (
            val currentState = _state.value
        ) {
            is SettingsState.ChangeEmail -> SettingsState.Main(
                email = currentState.email,
           )
            is SettingsState.ChangePassword -> SettingsState.Main(
                email = currentState.email,
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
            try {
                // todo logout
            } finally {
                withContext(dispatcherMain) {
                    _action.value = SettingsAction.NavigateToIntro
                }
            }
        }
    }

    private fun clear() {
        _state.value = SettingsState.Idle
        _action.value = null
    }
}
