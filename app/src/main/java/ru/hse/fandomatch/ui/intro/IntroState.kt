package ru.hse.fandomatch.ui.intro

sealed class IntroState {
    data object Main : IntroState()
    data object Idle : IntroState()
    data object Loading : IntroState()
}

sealed class IntroEvent {
    data object GoToRegistrationButtonClicked: IntroEvent()
    data object GoToLoginButtonClicked: IntroEvent()
    data object CheckPastLogin: IntroEvent()
    data object Clear: IntroEvent()
}

sealed class IntroAction {
    data object NavigateToMatches : IntroAction()
    data object NavigateToRegistration : IntroAction()
    data object NavigateToLogin : IntroAction()
}
