package ru.hse.fandomatch.ui.passwordrecovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.checkEmailContent
import ru.hse.fandomatch.checkPasswordContent
import ru.hse.fandomatch.checkPasswordLength
import ru.hse.fandomatch.domain.usecase.user.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.user.ResetPasswordUseCase
import java.lang.IllegalArgumentException

class PasswordRecoveryViewModel(
    private val getVerificationCodeUseCase: GetVerificationCodeUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<PasswordRecoveryState> =
        MutableStateFlow(PasswordRecoveryState.Email())
    val state: StateFlow<PasswordRecoveryState>
        get() = _state
    private val _action = MutableStateFlow<PasswordRecoveryAction?>(null)
    val action: StateFlow<PasswordRecoveryAction?>
        get() = _action

    fun obtainEvent(event: PasswordRecoveryEvent) {
        Log.d("PasswordRecoveryViewModel", "Event: $event")
        when (event) {
            is PasswordRecoveryEvent.EmailChanged -> emailChanged(event.email)
            PasswordRecoveryEvent.SendCodeClicked -> sendCode()
            is PasswordRecoveryEvent.NewPasswordChanged -> newPasswordChanged(event.password)
            is PasswordRecoveryEvent.RepeatNewPasswordChanged -> repeatNewPasswordChanged(event.password)
            PasswordRecoveryEvent.ToggleNewPasswordVisibility -> toggleNewPasswordVisibility()
            PasswordRecoveryEvent.ToggleRepeatNewPasswordVisibility -> toggleRepeatNewPasswordVisibility()
            is PasswordRecoveryEvent.SavePasswordClicked -> savePassword(event.code)
            is PasswordRecoveryEvent.Clear -> clear()
        }
    }

    private fun emailChanged(email: String) {
        val currentState = _state.value as? PasswordRecoveryState.Email ?: return
        val emailErr = if (!email.checkEmailContent())
            PasswordRecoveryState.PasswordRecoveryError.EMAIL_CONTENT
        else PasswordRecoveryState.PasswordRecoveryError.IDLE
        _state.value = currentState.copy(
            email = email,
            emailError = emailErr
        )
    }

    private fun sendCode() {
        val currentState = _state.value as? PasswordRecoveryState.Email ?: return
        if (currentState.email.isBlank()) {
            _state.value = currentState.copy(
                emailError = PasswordRecoveryState.PasswordRecoveryError.EMAIL_CONTENT
            )
            return
        }
        try {
            viewModelScope.launch(dispatcherIO) {
                getVerificationCodeUseCase.execute(currentState.email)
            }
        } catch (e: Exception) {
            Log.e("PasswordRecoveryViewModel", "Failed to send verification code", e)
             _state.value = currentState.copy(
                emailError = PasswordRecoveryState.PasswordRecoveryError.NETWORK
            )
            return
        }
        _state.value = PasswordRecoveryState.Main()
    }

    private fun newPasswordChanged(password: String) {
        val currentState = _state.value as? PasswordRecoveryState.Main ?: return
        val passwordErr = when {
            !password.checkPasswordLength() -> PasswordRecoveryState.PasswordRecoveryError.PASSWORD_LENGTH
            !password.checkPasswordContent() -> PasswordRecoveryState.PasswordRecoveryError.PASSWORD_CONTENT
            else -> PasswordRecoveryState.PasswordRecoveryError.IDLE
        }
        _state.value = currentState.copy(
            newPassword = password,
            newPasswordError = passwordErr,
        )
    }

    private fun repeatNewPasswordChanged(password: String) {
        val currentState = _state.value as? PasswordRecoveryState.Main ?: return
        val passwordErr = when {
            !password.checkPasswordLength() -> PasswordRecoveryState.PasswordRecoveryError.PASSWORD_LENGTH
            !password.checkPasswordContent() -> PasswordRecoveryState.PasswordRecoveryError.PASSWORD_CONTENT
            password != currentState.newPassword -> PasswordRecoveryState.PasswordRecoveryError.PASSWORD_MISMATCH
            else -> PasswordRecoveryState.PasswordRecoveryError.IDLE
        }
        _state.value = currentState.copy(
            repeatNewPassword = password,
            repeatNewPasswordError = passwordErr,
        )
    }

    private fun toggleNewPasswordVisibility() {
        val currentState = _state.value as? PasswordRecoveryState.Main ?: return
        _state.value = currentState.copy(
            newPasswordVisibility = !currentState.newPasswordVisibility
        )
    }

    private fun toggleRepeatNewPasswordVisibility() {
        val currentState = _state.value as? PasswordRecoveryState.Main ?: return
        _state.value = currentState.copy(
            repeatNewPasswordVisibility = !currentState.repeatNewPasswordVisibility
        )
    }

    private fun savePassword(code: String) {
        val currentState = _state.value as? PasswordRecoveryState.Main ?: return
        val resetState = currentState.copy(
            codeError = PasswordRecoveryState.PasswordRecoveryError.IDLE,
            newPasswordError = PasswordRecoveryState.PasswordRecoveryError.IDLE,
            repeatNewPasswordError = PasswordRecoveryState.PasswordRecoveryError.IDLE,
            isLoading = false,
        )

        if (code.isBlank()) {
            _state.value = resetState.copy(
                codeError = PasswordRecoveryState.PasswordRecoveryError.EMPTY_CODE,
            )
            return
        }

        if (!resetState.newPassword.checkPasswordLength()) {
            _state.value = resetState.copy(
                newPasswordError = PasswordRecoveryState.PasswordRecoveryError.PASSWORD_LENGTH,
            )
            return
        }

        if (!resetState.newPassword.checkPasswordContent()) {
            _state.value = resetState.copy(
                newPasswordError = PasswordRecoveryState.PasswordRecoveryError.PASSWORD_CONTENT,
            )
            return
        }

        if (!resetState.repeatNewPassword.checkPasswordLength()) {
            _state.value = resetState.copy(
                repeatNewPasswordError = PasswordRecoveryState.PasswordRecoveryError.PASSWORD_LENGTH,
            )
            return
        }

        if (!resetState.repeatNewPassword.checkPasswordContent()) {
            _state.value = resetState.copy(
                repeatNewPasswordError = PasswordRecoveryState.PasswordRecoveryError.PASSWORD_CONTENT,
            )
            return
        }

        if (resetState.newPassword != resetState.repeatNewPassword) {
            _state.value = resetState.copy(
                repeatNewPasswordError = PasswordRecoveryState.PasswordRecoveryError.PASSWORD_MISMATCH,
            )
            return
        }

        _state.value = resetState.copy(isLoading = true)
        viewModelScope.launch(dispatcherIO) {
            try {
                resetPasswordUseCase.execute(code, resetState.newPassword)
                withContext(dispatcherMain) {
                    _action.value = PasswordRecoveryAction.NavigateToAuthorization
                }
            } catch (_: IllegalArgumentException) {
                withContext(dispatcherMain) {
                    val latestState = _state.value as? PasswordRecoveryState.Main ?: return@withContext
                    _state.value = latestState.copy(
                        isLoading = false,
                        codeError = PasswordRecoveryState.PasswordRecoveryError.INVALID_CODE,
                    )
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    val latestState = _state.value as? PasswordRecoveryState.Main ?: return@withContext
                    _state.value = latestState.copy(
                        isLoading = false,
                        codeError = PasswordRecoveryState.PasswordRecoveryError.NETWORK,
                    )
                }
            }
        }
    }

    private fun clear() {
        _state.value = PasswordRecoveryState.Email()
        _action.value = null
    }
}
