package ru.hse.fandomatch.ui.filters

import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender

sealed class FiltersState {
    object Idle : FiltersState()
    object Loading : FiltersState()
    data class Main(
        val selectedGenders: List<Gender>,
        val ageRange: IntRange,
        val selectedCategories: List<FandomCategory>,
        val selectedFandoms: List<Fandom>,
        val foundFandoms: List<Fandom>,
        val onlyInUserCity: Boolean,
        val userCity: City?,
        val areFandomsLoading: Boolean,
    ) : FiltersState()
    data object Error : FiltersState()
}

sealed class FiltersEvent {
    data class GenderSelected(val gender: Gender) : FiltersEvent()
    data class AgeRangeChanged(val ageRange: IntRange) : FiltersEvent()
    data class CategoryToggled(val category: FandomCategory) : FiltersEvent()
    data class FandomAdded(val fandom: Fandom) : FiltersEvent()
    data class FandomRemoved(val fandom: Fandom) : FiltersEvent()
    data class FandomSearched(val query: String?) : FiltersEvent()
    data class LocationToggled(val isChecked: Boolean) : FiltersEvent()
    object ResetFilters : FiltersEvent()
    object ApplyFilters : FiltersEvent()
    object Clear : FiltersEvent()
    object LoadInitialFilters : FiltersEvent()
}

sealed class FiltersAction {
    object NavigateToMatches : FiltersAction()
    object ShowErrorToast : FiltersAction()
}

