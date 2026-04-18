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
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.data.mock.mockCities
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.checkDescriptionLength
import ru.hse.fandomatch.checkNameContent
import ru.hse.fandomatch.checkNameLength
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.fandoms.GetFandomsByQueryUseCase
import ru.hse.fandomatch.domain.usecase.user.EditProfileUseCase
import ru.hse.fandomatch.domain.usecase.user.GetCitiesByQueryUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase
import kotlin.collections.plus

class EditProfileViewModel(
    private val getFandomsByQueryUseCase: GetFandomsByQueryUseCase,
    private val getCitiesByQueryUseCase: GetCitiesByQueryUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val editProfileUseCase: EditProfileUseCase,
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
            is EditProfileEvent.CitySearched -> searchCity(event.query)
            is EditProfileEvent.CitySelected -> updateCity(event.city)
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
        val currentState = _state.value as? EditProfileState.Main ?: return
        if (avatar == null) return
        _state.value = currentState.copy(
            avatarBytes = avatar,
        )
    }

    private fun updateBackground(background: ByteArray?) {
        val currentState = _state.value as? EditProfileState.Main ?: return
        if (background == null) return
        _state.value = currentState.copy(
            backgroundBytes = background,
        )
    }

    private fun searchCity(query: String?) {
        val currentState = _state.value as? EditProfileState.Main ?: return
        if (query.isNullOrBlank()) {
            _state.value = currentState.copy(foundCities = emptyList(), areCitiesLoading = false)
            return
        }
        viewModelScope.launch(dispatcherIO) {
            _state.value = currentState.copy(foundCities = emptyList(), areCitiesLoading = true)
            val result = getCitiesByQueryUseCase.execute(query)
            val foundCities = result.getOrNull() ?: run {
                Log.e("EditProfileViewModel", "Failed to search cities: ${result.exceptionOrNull()}")
                withContext(dispatcherMain) {
                    _state.value = currentState.copy(foundCities = emptyList(), areCitiesLoading = false)
                }
                return@launch
            }
            _state.value = currentState.copy(foundCities = foundCities, areCitiesLoading = false)
        }
    }

    private fun updateCity(city: City?) {
        val currentState = _state.value as? EditProfileState.Main ?: return
        _state.value = currentState.copy(
            city = city,
        )
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
        val currentState = state.value as? EditProfileState.Main ?: return
        if (query.isNullOrBlank()) {
            _state.value = currentState.copy(foundFandoms = emptyList())
            return
        }
        _state.value = currentState.copy(foundFandoms = emptyList(), areFandomsLoading = true)
        viewModelScope.launch(dispatcherIO) {
            val result = getFandomsByQueryUseCase.execute(query)
            val foundFandoms = result.getOrNull() ?: run {
                Log.e(
                    "EditProfileViewModel",
                    "Failed to search fandoms: ${result.exceptionOrNull()}"
                )
                withContext(dispatcherMain) {
                    _state.value =
                        currentState.copy(foundFandoms = emptyList(), areFandomsLoading = false)
                }
                return@launch
            }
            withContext(dispatcherMain) {
                _state.value =
                    currentState.copy(foundFandoms = foundFandoms, areFandomsLoading = false)
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
            _state.value = EditProfileState.Loading
            delay(1000)
            val result = getUserUseCase.execute(null, true)
            val user = result.getOrNull() ?: run {
                Log.e("EditProfileViewModel", "Failed to load user data", result.exceptionOrNull())
                _state.value = EditProfileState.Error
                return@launch
            }
            _state.value = EditProfileState.Main(
                id = user.id,
                name = user.name,
                login = (user.profileType as ProfileType.Own).login,
                description = user.description,
                avatarBytes = null,
                avatar = user.avatar,
                backgroundBytes = null,
                background = user.background,
                fandoms = user.fandoms,
                foundFandoms = emptyList(),
                areFandomsLoading = false,
                city = user.city,
                foundCities = emptyList(),
                areCitiesLoading = false,
            )
        }
    }

    private fun saveData() {
        val currentState = _state.value as? EditProfileState.Main ?: return
        _state.value = EditProfileState.Loading
        viewModelScope.launch(dispatcherIO) {
            val avatarMediaId = currentState.avatarBytes?.let {
                val avatarResult = uploadMediaUseCase.execute(
                    bytes = currentState.avatarBytes,
                    mediaType = MediaType.IMAGE
                )
                val avatarMediaId = avatarResult.getOrNull() ?: run {
                    Log.e("EditProfileViewModel", "Failed to upload avatar", avatarResult.exceptionOrNull())
                    withContext(dispatcherMain) {
                        _state.value = currentState
                        _action.value = EditProfileAction.ShowErrorToast
                    }
                    return@launch
                }
                avatarMediaId
            } ?: currentState.avatar?.id
            val backgroundMediaId = currentState.backgroundBytes?.let {
                val backgroundResult = uploadMediaUseCase.execute(
                    bytes = currentState.backgroundBytes,
                    mediaType = MediaType.IMAGE
                )
                val backgroundMediaId = backgroundResult.getOrNull() ?: run {
                    Log.e("EditProfileViewModel", "Failed to upload background", backgroundResult.exceptionOrNull())
                    withContext(dispatcherMain) {
                        _state.value = currentState
                        _action.value = EditProfileAction.ShowErrorToast
                    }
                    return@launch
                }
                backgroundMediaId
            } ?: currentState.background?.id
            val result = editProfileUseCase.execute(
                name = currentState.name,
                bio = currentState.description,
                city = currentState.city,
                fandoms = currentState.fandoms,
                avatarMediaId = avatarMediaId,
                backgroundMediaId = backgroundMediaId,
            )
            if (result.isFailure) {
                Log.e(
                    "EditProfileViewModel",
                    "Failed to save profile data",
                    result.exceptionOrNull()
                )
                withContext(dispatcherMain) {
                    _state.value = currentState
                    _action.value = EditProfileAction.ShowErrorToast
                }
                return@launch
            }
            withContext(dispatcherMain) {
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
