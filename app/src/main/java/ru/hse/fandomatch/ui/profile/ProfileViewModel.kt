package ru.hse.fandomatch.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendRequestsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendsUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetUserPostsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase

class ProfileViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val likeOrDislikeProfileUseCase: LikeOrDislikeProfileUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val getFriendRequestsUseCase: GetFriendRequestsUseCase,
    val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ProfileState> = MutableStateFlow(ProfileState.Idle)
    val state: StateFlow<ProfileState> get() = _state
    private val _action = MutableStateFlow<ProfileAction?>(null)
    val action: StateFlow<ProfileAction?> get() = _action

    fun obtainEvent(event: ProfileEvent) {
        Log.i("ProfileViewModel", "Obtained event: $event")
        when (event) {
            is ProfileEvent.LoadProfile -> {
                loadProfile(event.userId, event.isCurrentUser)
            }
            is ProfileEvent.MessageButtonClicked -> {
                _action.value = ProfileAction.GoToMessages(event.userId)
            }
            ProfileEvent.EditProfileButtonClicked -> {
                _action.value = ProfileAction.GoToEditProfile
            }
            ProfileEvent.SettingsButtonClicked -> {
                _action.value = ProfileAction.GoToSettings
            }
            ProfileEvent.AddPostButtonClicked -> {
                _action.value = ProfileAction.GoToAddPost
            }
            is ProfileEvent.LikeButtonClicked -> {
                likeOrDislikeProfile(event.profileId, isLike = true)
            }
            is ProfileEvent.DislikeButtonClicked -> {
                likeOrDislikeProfile(event.profileId, isLike = false)
            }
            ProfileEvent.FriendsButtonClicked -> loadFriends()
            ProfileEvent.PostsButtonClicked -> loadPosts()
            ProfileEvent.RequestsButtonClicked -> loadRequests()
            is ProfileEvent.ProfileClicked -> {
                _action.value = ProfileAction.GoToProfile(event.profileId)
            }
            ProfileEvent.Clear -> clear()
        }
    }

    private fun loadProfile(userId: String?, isCurrentUser: Boolean) {
        // todo backend
        viewModelScope.launch(dispatcherIO) {
            val result = getUserUseCase.execute(userId, isCurrentUser)
            val user = result.getOrNull() ?: run {
                Log.e("ProfileViewModel", "Failed to load profile info", result.exceptionOrNull())
                withContext(dispatcherMain) {
                    _state.value = ProfileState.Error(ProfileState.ProfileError.NO_USER)
                }
                return@launch
            }
            val posts = getUserPostsUseCase.execute(userId, isCurrentUser)
            val type = user.profileType
            Log.i("ProfileViewModel", "Loading profile for userId: $userId")
            withContext(dispatcherMain) {
                _state.value = ProfileState.Main(
                    id = user.id,
                    login = when (type) {
                        is ProfileType.Stranger -> null
                        is ProfileType.Own -> type.login
                        is ProfileType.Friend -> type.login
                    },
                    fandoms = user.fandoms,
                    description = user.description,
                    name = user.name,
                    gender = user.gender,
                    age = user.age,
                    avatarUrl = user.avatarUrl,
                    backgroundUrl = user.backgroundUrl,
                    city = user.city,
                    type = type,
                    bottomSheetState = ProfileState.BottomSheetState.Posts(posts, false), // todo error handling
                )
            }
        }
    }

    private fun likeOrDislikeProfile(profileId: String, isLike: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            likeOrDislikeProfileUseCase.execute(profileId, isLike)
            withContext(dispatcherMain) {
                _action.value = ProfileAction.GoToMatches
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch(dispatcherIO) {
            val currentState = (_state.value as? ProfileState.Main) ?: return@launch
            val userId = currentState.id
            val isCurrentUser = currentState.type is ProfileType.Own
            val posts = getUserPostsUseCase.execute(userId, isCurrentUser)
            withContext(dispatcherMain) {
                _state.value = currentState.copy(
                    bottomSheetState = ProfileState.BottomSheetState.Posts(posts, false) // todo error handling
                )
            }
        }
    }

    private fun loadFriends() {
        val currentState = _state.value
        if (currentState !is ProfileState.Main) return
        viewModelScope.launch(dispatcherIO) {
            val friends = getFriendsUseCase.execute()
            withContext(dispatcherMain) {
                _state.value = currentState.copy(
                    bottomSheetState = ProfileState.BottomSheetState.Friends(friends, false) // todo error handling
                )
            }
        }
    }

    private fun loadRequests() {
        val currentState = _state.value
        if (currentState !is ProfileState.Main) return
        viewModelScope.launch(dispatcherIO) {
            val requests = getFriendRequestsUseCase.execute()
            withContext(dispatcherMain) {
                _state.value = currentState.copy(
                    bottomSheetState = ProfileState.BottomSheetState.Requests(requests, false) // todo error handling
                )
            }
        }
    }

    private fun clear() {
        _state.value = ProfileState.Idle
        _action.value = null
    }
}
