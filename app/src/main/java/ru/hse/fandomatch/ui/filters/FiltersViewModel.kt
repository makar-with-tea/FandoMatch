package ru.hse.fandomatch.ui.filters

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.usecase.fandoms.GetFandomsByQueryUseCase
import ru.hse.fandomatch.domain.usecase.matches.ApplyFiltersUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadInitialFiltersUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase

class FiltersViewModel(
    private val loadInitialFiltersUseCase: LoadInitialFiltersUseCase,
    private val applyFiltersUseCase: ApplyFiltersUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getFandomsByQueryUseCase: GetFandomsByQueryUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,) : ViewModel() {
    private val _state: MutableStateFlow<FiltersState> = MutableStateFlow(FiltersState.Idle)
    val state: StateFlow<FiltersState>
        get() = _state

    private val _action = MutableStateFlow<FiltersAction?>(null)
    val action: StateFlow<FiltersAction?>
        get() = _action

    fun obtainEvent(event: FiltersEvent) {
        Log.d("FiltersViewModel", "Event obtained: $event")
        when (event) {
            is FiltersEvent.LoadInitialFilters -> loadInitialFilters()
            is FiltersEvent.GenderSelected -> updateGender(event.gender)
            is FiltersEvent.AgeRangeChanged -> updateAgeRange(event.ageRange)
            is FiltersEvent.CategoryToggled -> toggleCategory(event.category)
            is FiltersEvent.FandomAdded -> addFandom(event.fandom)
            is FiltersEvent.FandomRemoved -> removeFandom(event.fandom)
            is FiltersEvent.FandomSearched -> searchFandom(event.query)
            is FiltersEvent.LocationToggled -> toggleLocation(event.isChecked)
            is FiltersEvent.ResetFilters -> resetFilters()
            is FiltersEvent.ApplyFilters -> applyFilters()
            FiltersEvent.ToastShown -> _action.value = null
            is FiltersEvent.Clear -> clear()
        }
    }

    private fun loadInitialFilters() {
        viewModelScope.launch {
            _state.value = FiltersState.Loading
            val result = loadInitialFiltersUseCase.execute()
            val filters = result.getOrNull() ?: run {
                Log.e("FiltersViewModel", "Failed to load initial filters: ${result.exceptionOrNull()}")
                _state.value = FiltersState.Error
                return@launch
            }
            val userResult = getUserUseCase.execute(profileId = null, isCurrentUser = true)
            val user = userResult.getOrNull() ?: run {
                Log.e("FiltersViewModel", "Failed to load user data: ${userResult.exceptionOrNull()}")
                _state.value = FiltersState.Error
                return@launch
            }
            val city = user.city
            val initialFilters = FiltersState.Main(
                selectedGenders = filters.genders,
                ageRange = filters.minAge..filters.maxAge,
                selectedCategories = filters.categories,
                selectedFandoms = filters.fandoms,
                foundFandoms = emptyList(),
                onlyInUserCity = filters.onlyInUserCity,
                areFandomsLoading = false,
                userCity = city,
            )
            _state.value = initialFilters
        }
    }

    private fun updateGender(gender: Gender) {
        val currentState = state.value
        if (currentState is FiltersState.Main) {
            _state.value = currentState.copy(
                selectedGenders =
                    if (gender in currentState.selectedGenders) {
                        if (currentState.selectedGenders.size == 1) {
                            currentState.selectedGenders
                        } else currentState.selectedGenders - gender
                    } else {
                        currentState.selectedGenders + gender
                    }
            )
        }
    }

    private fun updateAgeRange(ageRange: IntRange) {
        val currentState = state.value
        if (currentState is FiltersState.Main) {
            _state.value = currentState.copy(ageRange = ageRange)
        }
    }

    private fun toggleCategory(category: FandomCategory) {
        val currentState = state.value
        if (currentState is FiltersState.Main) {
            val updatedCategories = if (currentState.selectedCategories.contains(category)) {
                currentState.selectedCategories - category
            } else {
                currentState.selectedCategories + category
            }
            _state.value = currentState.copy(selectedCategories = updatedCategories)
        }
    }

    private fun addFandom(fandom: Fandom) {
        val currentState = state.value
        if (currentState is FiltersState.Main && fandom !in currentState.selectedFandoms) {
            _state.value = currentState.copy(selectedFandoms = currentState.selectedFandoms + fandom)
        }
    }

    private fun removeFandom(fandom: Fandom) {
        val currentState = state.value
        if (currentState is FiltersState.Main) {
            _state.value = currentState.copy(selectedFandoms = currentState.selectedFandoms - fandom)
        }
    }

    private fun searchFandom(query: String?) {
        val currentState = state.value
        if (currentState is FiltersState.Main) {
            if (query.isNullOrBlank()) {
                _state.value = currentState.copy(foundFandoms = emptyList())
                return
            }
            viewModelScope.launch(dispatcherIO) {
                _state.value = currentState.copy(foundFandoms = emptyList(), areFandomsLoading = true)
                val result = getFandomsByQueryUseCase.execute(query)
                val foundFandoms = result.getOrNull() ?: run {
                    Log.e("FiltersViewModel", "Failed to search fandoms: ${result.exceptionOrNull()}")
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(foundFandoms = emptyList(), areFandomsLoading = false)
                    }
                    return@launch
                }
                _state.value = currentState.copy(foundFandoms = foundFandoms, areFandomsLoading = false)
            }
        }
    }

    private fun toggleLocation(isChecked: Boolean) {
        val currentState = _state.value
        if (currentState is FiltersState.Main) {
            _state.value = currentState.copy(onlyInUserCity = isChecked)
        }
    }

    private fun resetFilters() {
        val currentState = _state.value
        if (currentState is FiltersState.Main) {
            val filters = Filters()
            val city = currentState.userCity
            _state.value = FiltersState.Main(
                selectedGenders = filters.genders,
                ageRange = filters.minAge..filters.maxAge,
                selectedCategories = filters.categories,
                selectedFandoms = filters.fandoms,
                foundFandoms = emptyList(),
                onlyInUserCity = filters.onlyInUserCity,
                userCity = city,
                areFandomsLoading = false,
            )
        }
    }

    private fun applyFilters() {
        val currentState = _state.value as? FiltersState.Main ?: return
        viewModelScope.launch {
            val result = applyFiltersUseCase.execute(
                genders = currentState.selectedGenders,
                minAge = currentState.ageRange.first,
                maxAge = currentState.ageRange.last,
                categories = currentState.selectedCategories,
                fandoms = currentState.selectedFandoms,
                onlyInUserCity = currentState.onlyInUserCity,
            )
            if (result.isFailure) {
                Log.e("FiltersViewModel", "Failed to apply filters: ${result.exceptionOrNull()}")
                _action.value = FiltersAction.ShowErrorToast
                return@launch
            }
            withContext(dispatcherMain) {
                _action.value = FiltersAction.NavigateToMatches
            }
        }
    }

    private fun clear() {
        _state.value = FiltersState.Idle
        _action.value = null
    }
}

internal const val MIN_AGE_IN_YEARS = 16
internal const val MAX_AGE_IN_YEARS = 40