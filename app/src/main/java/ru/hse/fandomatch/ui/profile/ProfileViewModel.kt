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
import ru.hse.fandomatch.data.mock.mockPosts
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.data.mock.mockUserPosts
import ru.hse.fandomatch.data.mock.mockUsers
import ru.hse.fandomatch.domain.exception.LoadDataException
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserIdUseCase
import ru.hse.fandomatch.ui.matches.MatchesState

class ProfileViewModel(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val likeOrDislikeProfileUseCase: LikeOrDislikeProfileUseCase,
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
                loadProfile(event.userId)
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
            ProfileEvent.Clear -> {
                clear()
            }
        }
    }

    private fun loadProfile(userId: String?) {
        // todo backend
        val user = if (userId == mockUser.id || userId == null) {
            mockUser
        } else {
            mockUsers.firstOrNull { it.id == userId }
        }
        if (user == null) {
            Log.e("ProfileViewModel", "User with id $userId not found")
            _state.value = ProfileState.Error(ProfileState.ProfileError.NO_USER)
            return
        }
        val type = user.profileType
        Log.i("ProfileViewModel", "Loading profile for userId: $userId")
        _state.value = ProfileState.Main(
            id = user.id,
            login = when (type) {
                ProfileType.Stranger -> null
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
            posts = when (type) {
                is ProfileType.Own -> mockUserPosts
                is ProfileType.Friend -> mockPosts.filter { it.authorId == user.id }
                ProfileType.Stranger -> mockPosts
                    .filter { it.authorId == user.id }
                    .map { it.copy(authorLogin = null) }
            }
        )
    }

    private fun likeOrDislikeProfile(profileId: String, isLike: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            val userId = getUserIdUseCase.execute()
            userId?.let {
                likeOrDislikeProfileUseCase.execute(profileId, isLike)
            }
            withContext(dispatcherMain) {
                _action.value = ProfileAction.GoToMatches
            }
        }
    }

    private fun clear() {
        _state.value = ProfileState.Idle
        _action.value = null
    }
}
