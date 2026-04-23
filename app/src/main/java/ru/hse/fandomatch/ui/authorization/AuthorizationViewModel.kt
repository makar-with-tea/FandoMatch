package ru.hse.fandomatch.ui.authorization

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.usecase.auth.LoginUseCase

class AuthorizationViewModel(
    private val loginUseCase: LoginUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<AuthorizationState> =
        MutableStateFlow(AuthorizationState.Main())
    val state: StateFlow<AuthorizationState>
        get() = _state
    private val _action = MutableStateFlow<AuthorizationAction?>(null)
    val action: StateFlow<AuthorizationAction?>
        get() = _action

    fun obtainEvent(event: AuthorizationEvent) {
        Log.d("AuthorizationViewModel", "Event: $event")
        when (event) {
            AuthorizationEvent.LoginButtonClicked -> login()
            is AuthorizationEvent.LoginChanged -> loginChanged(event.login)
            is AuthorizationEvent.PasswordChanged -> passwordChanged(event.password)
            AuthorizationEvent.ShowPasswordButtonClicked -> changeVisibilityState()
            AuthorizationEvent.ForgotPasswordButtonClicked -> forgotPassword()
            is AuthorizationEvent.Clear -> clear()
        }
    }

    private fun loginChanged(login: String) {
        val currentState = _state.value as? AuthorizationState.Main ?: return
        val loginError = if (login.isEmpty()) AuthorizationState.AuthorizationError.EMPTY_LOGIN
        else AuthorizationState.AuthorizationError.IDLE
        _state.value = currentState.copy(
            login = login,
            loginError = loginError,
        )
    }

    private fun passwordChanged(password: String) {
        val currentState = _state.value as? AuthorizationState.Main ?: return
        val passwordError =
            if (password.isEmpty()) AuthorizationState.AuthorizationError.EMPTY_PASSWORD
            else AuthorizationState.AuthorizationError.IDLE
        _state.value = currentState.copy(
            password = password,
            passwordError = passwordError,
        )
    }

    private fun login() {
        var currentState = _state.value as? AuthorizationState.Main ?: return
        _state.value = currentState.copy(isLoading = true)
        var isError = false

        if (currentState.login.isEmpty()) {
            currentState = currentState.copy(
                loginError = AuthorizationState.AuthorizationError.EMPTY_LOGIN,
                isLoading = false,
            )
            isError = true
        }

        if (currentState.password.isEmpty()) {
            currentState = currentState.copy(
                passwordError = AuthorizationState.AuthorizationError.EMPTY_PASSWORD,
                isLoading = false
            )
            isError = true
        }

        if (isError) {
            _state.value = currentState.copy(
                isLoading = false,
            )
            return
        }

        viewModelScope.launch(dispatcherIO) {
            loginUseCase.execute(currentState.login, currentState.password)
                .onFailure { e ->
                    withContext(dispatcherMain) {
                        Log.e("AuthorizationViewModel", "Login failed", e)
                        // todo корректная ошибка (даша?)
                        if (e is InvalidCredentialsException) {
                            _state.value = currentState.copy(
                                loginError = AuthorizationState.AuthorizationError.IDLE,
                                passwordError = AuthorizationState.AuthorizationError.INVALID_CREDENTIALS,
                                isLoading = false,
                            )
                        } else {
                            _state.value = currentState.copy(
                                passwordError = AuthorizationState.AuthorizationError.NETWORK,
                                loginError = AuthorizationState.AuthorizationError.NETWORK,
                                isLoading = false,
                            )
                        }
                    }
                }
                .onSuccess {
                    withContext(dispatcherMain) {
                        _action.value = AuthorizationAction.NavigateToMatches
                    }
                }
        }
    }

    private fun forgotPassword() {
        _action.value = AuthorizationAction.NavigateToPasswordRecovery
    }

    private fun clear() {
        _state.value = AuthorizationState.Main()
        _action.value = null
    }

    private fun changeVisibilityState() {
        if (_state.value is AuthorizationState.Main) {
            _state.value = (_state.value as AuthorizationState.Main).copy(
                passwordVisibility = !(_state.value as AuthorizationState.Main).passwordVisibility
            )
        }
    }
}
