package ru.hse.fandomatch.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.hse.fandomatch.data.mock.mockFandoms
import ru.hse.fandomatch.data.mock.mockFilters
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender

class FiltersViewModel(
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,) : ViewModel() {
    private val _state: MutableStateFlow<FiltersState> = MutableStateFlow(FiltersState.Idle)
    val state: StateFlow<FiltersState>
        get() = _state

    private val _action = MutableStateFlow<FiltersAction?>(null)
    val action: StateFlow<FiltersAction?>
        get() = _action

    private fun loadInitialFilters() {
        viewModelScope.launch {
            _state.value = FiltersState.Loading
            delay(1000)

            val initialFilters = FiltersState.Main(
                selectedGenders = mockFilters.genders,
                ageRange = mockFilters.minAge..mockFilters.maxAge,
                selectedCategories = mockFilters.categories,
                selectedFandoms = mockFilters.fandoms,
                foundFandoms = emptyList(),
                onlyInUserCity = mockFilters.onlyInUserCity,
                userCity = mockFilters.userCity,
                areFandomsLoading = false,
            )
            _state.value = initialFilters
        }
    }

    fun obtainEvent(event: FiltersEvent) {
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
            is FiltersEvent.Clear -> clear()
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
                // todo
                _state.value = currentState.copy(foundFandoms = emptyList(), areFandomsLoading = true)
                delay(500)
                val foundFandoms = mockFandoms.filter { it.name.contains(query, ignoreCase = true) }
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
            val filters = Filters(userCity = currentState.userCity)
            _state.value = FiltersState.Main(
                selectedGenders = filters.genders,
                ageRange = filters.minAge..filters.maxAge,
                selectedCategories = filters.categories,
                selectedFandoms = filters.fandoms,
                foundFandoms = emptyList(),
                onlyInUserCity = filters.onlyInUserCity,
                userCity = filters.userCity,
                areFandomsLoading = false,
            )
        }
    }

    private fun applyFilters() {
        _action.value = FiltersAction.NavigateToMatches
    }

    private fun clear() {
        _state.value = FiltersState.Idle
        _action.value = null
    }
}

internal const val MIN_AGE_IN_YEARS = 16
internal const val MAX_AGE_IN_YEARS = 40