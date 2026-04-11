package ru.hse.fandomatch.ui.profile

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileType

sealed class ProfileState {
    enum class ProfileError {
        IDLE,
        NETWORK,
        NO_USER,
    }
    data class Main(
        val id: String,
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
    data class LoadProfile(val userId: String?) : ProfileEvent()
    data class MessageButtonClicked(val userId: String?) : ProfileEvent()
    data object EditProfileButtonClicked : ProfileEvent()
    data object SettingsButtonClicked : ProfileEvent()
    data object AddPostButtonClicked : ProfileEvent()
    data class LikeButtonClicked(val profileId: String) : ProfileEvent()
    data class DislikeButtonClicked(val profileId: String) : ProfileEvent()
    data object Clear : ProfileEvent()
}

sealed class ProfileAction {
    data class GoToMessages(val userId: String?) : ProfileAction()
    data object GoToEditProfile : ProfileAction()
    data object GoToSettings : ProfileAction()
    data object GoToAddPost : ProfileAction()
    data object GoToMatches : ProfileAction()
}
