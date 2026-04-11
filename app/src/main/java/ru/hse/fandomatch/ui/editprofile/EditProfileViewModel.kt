package ru.hse.fandomatch.ui.editprofile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.hse.fandomatch.data.mock.mockCities
import ru.hse.fandomatch.data.mock.mockFandoms
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.checkDescriptionLength
import ru.hse.fandomatch.checkNameContent
import ru.hse.fandomatch.checkNameLength
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.getName
import kotlin.collections.plus

class EditProfileViewModel(
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    private val _state: MutableStateFlow<EditProfileState> = MutableStateFlow(EditProfileState.Idle)
    val state: StateFlow<EditProfileState>
        get() = _state

    private val _action = MutableStateFlow<EditProfileAction?>(null)
    val action: StateFlow<EditProfileAction?>
        get() = _action

    fun obtainEvent(event: EditProfileEvent) {
        Log.d("EditProfileViewModel", "Obtained event: $event")
        when (event) {
            EditProfileEvent.AddFandomButtonClicked -> goToAddFandom()
            is EditProfileEvent.AvatarChanged -> updateAvatar(event.avatar)
            is EditProfileEvent.BackgroundChanged -> updateBackground(event.background)
            is EditProfileEvent.CityChanged -> updateCity(event.cityName)
            is EditProfileEvent.DescriptionChanged -> updateDescription(event.description)
            is EditProfileEvent.FandomAdded -> addFandom(event.fandom)
            is EditProfileEvent.FandomRemoved -> removeFandom(event.fandom)
            is EditProfileEvent.FandomSearched -> searchFandom(event.query)
            is EditProfileEvent.NameChanged -> updateName(event.name)
            EditProfileEvent.SaveButtonClicked -> saveData()
            EditProfileEvent.LoadProfileData -> loadProfileData()
            EditProfileEvent.Clear -> clear()
        }
    }

    private fun goToAddFandom() {
        _action.value = EditProfileAction.NavigateToAddFandom
    }

    private fun updateAvatar(avatar: ByteArray?) {
        val currentState = _state.value
        if (avatar == null) return
        viewModelScope.launch(dispatcherIO) {
            delay(1000)
            val avatarUrl = "luffy" // todo upload avatar and get url + handle error
            if (currentState is EditProfileState.Main) {
                _state.value = currentState.copy(
                    avatarUrl = avatarUrl,
                )
            }
        }
    }

    private fun updateBackground(background: ByteArray?) {
        val currentState = _state.value
        if (background == null) return
        viewModelScope.launch(dispatcherIO) {
            delay(1000)
            val backgroundUrl = "i_may_be_stupid" // todo upload background and get url + handle error
            if (currentState is EditProfileState.Main) {
                _state.value = currentState.copy(
                    backgroundUrl = backgroundUrl,
                )
            }
        }
    }


    private fun updateCity(cityName: String) {
        val currentState = _state.value
        if (currentState is EditProfileState.Main) {
            // todo get cities from backend and handle error
            val cityNames = mockCities.map(City::nameRussian) + mockCities.map(City::nameEnglish)
            val cityError = when {
                cityName.isBlank() -> EditProfileState.EditProfileError.IDLE
                cityName !in cityNames -> EditProfileState.EditProfileError.CITY_NOT_FOUND
                else -> EditProfileState.EditProfileError.IDLE
            }
            val city = mockCities.find { it.nameRussian.equals(cityName, ignoreCase = true) || it.nameEnglish.equals(cityName, ignoreCase = true) }
            _state.value = currentState.copy(
                city = city,
                cityError = cityError,
            )
        }
    }

    private fun updateDescription(description: String?) {
        val currentState = _state.value
        val descriptionErr = when {
            description != null && !description.checkDescriptionLength() -> EditProfileState.EditProfileError.DESCRIPTION_LENGTH
            else -> EditProfileState.EditProfileError.IDLE
        }

        if (currentState is EditProfileState.Main) {
            _state.value = currentState.copy(
                description = description,
                descriptionError = descriptionErr,
            )
        }
    }

    private fun addFandom(fandom: Fandom) {
        val currentState = state.value
        if (currentState is EditProfileState.Main && fandom !in currentState.fandoms) {
            _state.value = currentState.copy(fandoms = currentState.fandoms + fandom)
        }
    }

    private fun removeFandom(fandom: Fandom) {
        val currentState = state.value
        if (currentState is EditProfileState.Main) {
            _state.value = currentState.copy(fandoms = currentState.fandoms - fandom)
        }
    }

    private fun searchFandom(query: String?) {
        val currentState = state.value
        if (currentState is EditProfileState.Main) {
            if (query.isNullOrBlank()) {
                _state.value = currentState.copy(foundFandoms = emptyList())
                return
            }
            viewModelScope.launch(dispatcherIO) {
                // todo
                _state.value = currentState.copy(foundFandoms = emptyList(), areFandomsLoading = true)
                delay(500)
                val foundFandoms = mockFandoms.filter { it.name.contains(query, ignoreCase = true) }
                _state.value = currentState.copy(foundFandoms = foundFandoms, areFandomsLoading = false)
            }
        }
    }

    private fun updateName(name: String) {
        val nameErr = when {
            !name.checkNameLength() -> EditProfileState.EditProfileError.NAME_LENGTH
            !name.checkNameContent() -> EditProfileState.EditProfileError.NAME_CONTENT
            else -> EditProfileState.EditProfileError.IDLE
        }
        val currentState = _state.value

        if (currentState is EditProfileState.Main) {
            _state.value = currentState.copy(
                name = name,
                nameError = nameErr
            )
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch(dispatcherIO) {
            // todo take from sharedPreferences current user id and take info from backend
            // todo handle error
            _state.value = EditProfileState.Loading
            delay(1000)
            val user = mockUser
            _state.value = EditProfileState.Main(
                id = mockUser.id,
                name = user.name,
                login = (user.profileType as ProfileType.Own).login,
                description = user.description,
                avatarUrl = user.avatarUrl,
                backgroundUrl = user.backgroundUrl,
                fandoms = user.fandoms,
                foundFandoms = emptyList(),
                areFandomsLoading = false,
                city = user.city,
            )
        }
    }

    private fun saveData() {
        val currentState = _state.value
        if (currentState is EditProfileState.Main) {
            viewModelScope.launch(dispatcherIO) {
                _state.value = EditProfileState.Loading
                mockUser = mockUser.copy(
                    name = currentState.name,
                    description = currentState.description,
                    avatarUrl = currentState.avatarUrl,
                    backgroundUrl = currentState.backgroundUrl,
                    fandoms = currentState.fandoms,
                    city = currentState.city,
                )
                delay(1000)
                // todo save data + handle error (do not navigate back if error occurred)
                _state.value = currentState
                _action.value = EditProfileAction.NavigateToMyProfile
            }
        }
    }

    private fun clear() {
        _state.value = EditProfileState.Idle
        _action.value = null
    }
}
