package ru.hse.fandomatch.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.data.mock.mockPosts
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.data.mock.mockUserPosts
import ru.hse.fandomatch.data.mock.mockUsers

class ProfileViewModel(
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
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
            ProfileEvent.Clear -> {
                clear()
            }
        }
    }

    private fun loadProfile(userId: Long?) {
        // todo backend
        val user = if (userId == mockUser.id || userId == null) {
            mockUser
        } else {
            mockUsers.firstOrNull { it.id == userId }
        }
        val type = when (userId) {
            mockUser.id, null -> ProfileType.OWN
            mockUsers[0].id -> ProfileType.FRIEND
            else -> ProfileType.OTHER
        }
        if (user == null) {
            Log.e("ProfileViewModel", "User with id $userId not found")
            _state.value = ProfileState.Error(ProfileState.ProfileError.NO_USER)
            return
        }
        Log.i("ProfileViewModel", "Loading profile for userId: $userId")
        _state.value = ProfileState.Main(
            id = user.id,
            login = when (type) {
                ProfileType.OWN, ProfileType.FRIEND -> user.login
                ProfileType.OTHER -> null
            },
            fandoms = user.fandoms,
            description = user.description,
            name = user.name,
            gender = user.gender,
            age = calculateAge(user.birthDate),
            avatarUrl = user.avatarUrl,
            backgroundUrl = user.backgroundUrl,
            city = user.city,
            type = type,
            posts = when (type) {
                ProfileType.OWN -> mockUserPosts
                ProfileType.FRIEND -> mockPosts.filter { it.authorId == user.id }
                ProfileType.OTHER -> mockPosts
                    .filter { it.authorId == user.id }
                    .map { it.copy(authorLogin = null) }
            }
        )
    }

    private fun clear() {
        _state.value = ProfileState.Idle
        _action.value = null
    }

    private fun calculateAge(birthDate: java.time.LocalDate): Int {
        val today = java.time.LocalDate.now()
        var age = today.year - birthDate.year
        if (today.dayOfYear < birthDate.dayOfYear) {
            age--
        }
        return age
    }
}
