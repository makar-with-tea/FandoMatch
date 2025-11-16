package ru.hse.fandomatch.ui.authorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationViewModel(
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<AuthorizationState> =
        MutableStateFlow(AuthorizationState.Idle)
    val state: StateFlow<AuthorizationState>
        get() = _state
    private val _action = MutableStateFlow<AuthorizationAction?>(null)
    val action: StateFlow<AuthorizationAction?>
        get() = _action

    fun obtainEvent(event: AuthorizationEvent) {
        when (event) {
            is AuthorizationEvent.LoginButtonClicked -> {
                login(event.login, event.password)
            }

            is AuthorizationEvent.RegistrationButtonClicked -> {
                register(event.login, event.password)
            }

            is AuthorizationEvent.ShowPasswordButtonClicked -> {
                changeVisibilityState()
            }

            is AuthorizationEvent.Clear -> clear()
            is AuthorizationEvent.CheckPastLogin -> checkPastLogin()
        }
    }

    private fun login(login: String, password: String) {
        _state.value = AuthorizationState.Main(
            login = login,
            password = password,
            isLoading = true
        )
        var isError = false

        if (login.isEmpty()) {
            _state.value = (_state.value as AuthorizationState.Main).copy(
                loginError = AuthorizationState.AuthorizationError.EMPTY_LOGIN,
                isLoading = false
            )
            isError = true
        }

        if (password.isEmpty()) {
            _state.value = (_state.value as AuthorizationState.Main).copy(
                passwordError = AuthorizationState.AuthorizationError.EMPTY_PASSWORD,
                isLoading = false
            )
            isError = true
        }

        if (isError) {
            return
        }

        viewModelScope.launch(dispatcherIO) {
            try {
                withContext(dispatcherMain) {
                    _action.value = AuthorizationAction.NavigateToMatches
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = (_state.value as AuthorizationState.Main).copy(
                        passwordError =
                            AuthorizationState.AuthorizationError.NETWORK,
                        loginError = AuthorizationState.AuthorizationError.NETWORK,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun clear() {
        _state.value = AuthorizationState.Idle
        _action.value = null
    }

    private fun register(login: String, password: String) {
        _state.value = AuthorizationState.Main(
            login = login,
            password = password
        )
        _action.value = AuthorizationAction.NavigateToRegistration
    }

    private fun changeVisibilityState() {
        if (_state.value is AuthorizationState.Main) {
            _state.value = (_state.value as AuthorizationState.Main).copy(
                passwordVisibility = !(_state.value as AuthorizationState.Main).passwordVisibility
            )
        }
    }

    private fun checkPastLogin() {
        _state.value = AuthorizationState.Loading
        viewModelScope.launch(dispatcherIO) {
            val username = null
            // todo usecase
            withContext(dispatcherMain) {
                if (username != null) {
                    _action.value = AuthorizationAction.NavigateToMatches
                } else {
                    _state.value = AuthorizationState.Main()
                }
            }
        }
    }
}