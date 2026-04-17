package ru.hse.fandomatch.ui.profile

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileType

sealed class ProfileState {
    enum class ProfileError {
        IDLE,
        NETWORK,
        NO_USER,
    }
    sealed interface BottomSheetState {
        val isError: Boolean
        data class Posts(
            val posts: List<Post>,
            override val isError: Boolean
        ) : BottomSheetState
        data class Friends(
            val friends: List<OtherProfileItem>,
            override val isError: Boolean
        ) : BottomSheetState
        data class Requests(
            val requests: List<OtherProfileItem>,
            override val isError: Boolean
        ) : BottomSheetState
    }
    data class Main(
        val id: String,
        val login: String?,
        val fandoms: List<Fandom>,
        val description: String? = null,
        val name: String,
        val gender: Gender,
        val age: Int,
        val avatarUrl: String? = null,
        val backgroundUrl: String? = null,
        val city: City? = null,
        val type: ProfileType,
        val bottomSheetState: BottomSheetState,
    ) : ProfileState()

    data object Loading : ProfileState()

    data object Idle : ProfileState()

    data object Error : ProfileState()
}

sealed class ProfileEvent {
    data class LoadProfile(val userId: String?, val isCurrentUser: Boolean) : ProfileEvent()
    data class MessageButtonClicked(val userId: String?) : ProfileEvent()
    data object EditProfileButtonClicked : ProfileEvent()
    data object SettingsButtonClicked : ProfileEvent()
    data object AddPostButtonClicked : ProfileEvent()
    data class LikeButtonClicked(val profileId: String) : ProfileEvent()
    data class DislikeButtonClicked(val profileId: String) : ProfileEvent()
    data object PostsButtonClicked : ProfileEvent()
    data object FriendsButtonClicked : ProfileEvent()
    data object RequestsButtonClicked : ProfileEvent()
    data class ProfileClicked(val profileId: String) : ProfileEvent()
    data class PostClicked(val postId: String) : ProfileEvent()
    data class PostLiked(val postId: String) : ProfileEvent()
    data object Clear : ProfileEvent()
}

sealed class ProfileAction {
    data class GoToMessages(val userId: String?) : ProfileAction()
    data object GoToEditProfile : ProfileAction()
    data object GoToSettings : ProfileAction()
    data object GoToAddPost : ProfileAction()
    data object GoToMatches : ProfileAction()
    data class GoToProfile(val profileId: String) : ProfileAction()
    data class GoToPost(val postId: String) : ProfileAction()
}
