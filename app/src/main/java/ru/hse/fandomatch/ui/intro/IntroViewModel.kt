package ru.hse.fandomatch.ui.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.usecase.auth.GetPastLoginUseCase

class IntroViewModel(
    private val getPastLoginUseCase: GetPastLoginUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<IntroState> =
        MutableStateFlow(IntroState.Idle)
    val state: StateFlow<IntroState>
        get() = _state
    private val _action = MutableStateFlow<IntroAction?>(null)
    val action: StateFlow<IntroAction?>
        get() = _action

    fun obtainEvent(event: IntroEvent) {
        when (event) {
            is IntroEvent.GoToLoginButtonClicked -> {
                goToLogin()
            }

            is IntroEvent.GoToRegistrationButtonClicked -> {
                goToRegister()
            }
            is IntroEvent.Clear -> clear()
            is IntroEvent.CheckPastLogin -> checkPastLogin()
        }
    }

    private fun goToLogin() {
        _state.value = IntroState.Main
        _action.value = IntroAction.NavigateToLogin
    }

    private fun clear() {
        viewModelScope.launch(dispatcherIO) {
            delay(1000)
            _state.value = IntroState.Idle
            _action.value = null
        }
    }

    private fun goToRegister() {
        _state.value = IntroState.Main
        _action.value = IntroAction.NavigateToRegistration
    }

    private fun checkPastLogin() {
        viewModelScope.launch(dispatcherIO) {
            val userId = getPastLoginUseCase.execute()
            withContext(dispatcherMain) {
                if (userId != null) {
                    _action.value = IntroAction.NavigateToMatches
                } else {
                    _state.value = IntroState.Main
                }
            }
        }
    }
}
