package ru.hse.fandomatch.ui.myprofile

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Post

sealed class ProfileState {
    enum class ProfileError {
        IDLE,
        NETWORK,
        NO_USER,
    }
    data class Main(
        val id: Long,
        val login: String?,
        val fandoms: List<Fandom>,
        val description: String? = null,
        val name: String,
        val gender: Gender? = null,
        val age: Int,
        val avatarUrl: String? = null,
        val backgroundUrl: String? = null,
        val city: City? = null,
        val type: ProfileType,
        val posts: List<Post> = emptyList(), // todo pagination
    ) : ProfileState()

    data object Loading : ProfileState()

    data object Idle : ProfileState()

    data class Error(val error: ProfileError) : ProfileState()
}

sealed class ProfileEvent {
    data class LoadProfile(val userId: Long?) : ProfileEvent()
    data class MessageButtonClicked(val userId: Long?) : ProfileEvent()
    data object EditProfileButtonClicked : ProfileEvent()
    data object Clear : ProfileEvent()
}

sealed class ProfileAction {
    data class GoToMessages(val userId: Long?) : ProfileAction()
    data object GoToEditProfile : ProfileAction()
}

enum class ProfileType {
    OWN,
    FRIEND,
    OTHER,
}
