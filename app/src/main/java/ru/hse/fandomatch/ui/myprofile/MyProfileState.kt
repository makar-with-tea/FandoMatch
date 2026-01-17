package ru.hse.fandomatch.ui.myprofile

import ru.hse.fandomatch.domain.model.User

sealed class MyProfileState {
    enum class MyProfileError {
        IDLE,
        NETWORK,
    }
    data class Main(
        val user: User,
        val error: MyProfileError = MyProfileError.IDLE,
    ) : MyProfileState()

    data object Loading : MyProfileState()
}

sealed class MyProfileEvent {
}

sealed class MyProfileAction {
}
