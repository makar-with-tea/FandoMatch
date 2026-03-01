package ru.hse.fandomatch.ui.filters

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.ui.composables.FandomInput
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyCheckBox
import ru.hse.fandomatch.ui.intro.IntroAction
import ru.hse.fandomatch.ui.intro.IntroEvent
import ru.hse.fandomatch.ui.utils.getColor
import ru.hse.fandomatch.ui.utils.getName
import ru.hse.fandomatch.ui.utils.stringId
import ru.hse.fandomatch.ui.utils.toStringId


@Composable
fun FiltersScreen(
    navigateToMatches: () -> Unit,
    viewModel: FiltersViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("FiltersScreen", "State: $state")
    when (action.value) {
        is FiltersAction.NavigateToMatches -> {
            navigateToMatches()
            viewModel.obtainEvent(FiltersEvent.Clear)
        }

        null -> {}
    }

    when (val currentState = state.value) {
        is FiltersState.Main -> {
            MainState(
                state = currentState,
                onGenderSelected = { viewModel.obtainEvent(FiltersEvent.GenderSelected(it)) },
                onAgeRangeChanged = { viewModel.obtainEvent(FiltersEvent.AgeRangeChanged(it)) },
                onCategoryToggled = { viewModel.obtainEvent(FiltersEvent.CategoryToggled(it)) },
                onFandomAdded = { viewModel.obtainEvent(FiltersEvent.FandomAdded(it)) },
                onFandomRemoved = { viewModel.obtainEvent(FiltersEvent.FandomRemoved(it)) },
                onFandomSearch = { viewModel.obtainEvent(FiltersEvent.FandomSearched(it)) },
                onLocationToggled = { viewModel.obtainEvent(FiltersEvent.LocationToggled(it)) },
                onResetFilters = { viewModel.obtainEvent(FiltersEvent.ResetFilters) },
                onApplyFilters = { viewModel.obtainEvent(FiltersEvent.ApplyFilters) }
            )
        }
        is FiltersState.Loading -> {
            LoadingState()
        }
        is FiltersState.Idle -> {
            IdleState()
            viewModel.obtainEvent(FiltersEvent.LoadInitialFilters)
        }
    }
}

@Composable
private fun MainState(
    state: FiltersState.Main,
    onGenderSelected: (Gender) -> Unit,
    onAgeRangeChanged: (IntRange) -> Unit,
    onCategoryToggled: (FandomCategory) -> Unit,
    onFandomAdded: (Fandom) -> Unit,
    onFandomRemoved: (Fandom) -> Unit,
    onFandomSearch: (String?) -> Unit,
    onLocationToggled: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.gender_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MultiChoiceSegmentedButtonRow {
                    Gender.entries.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = Gender.entries.size
                            ),
                            onCheckedChange = { onGenderSelected(label) },
                            checked = label in state.selectedGenders,
                            label = { Text(stringResource(label.stringId())) }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.age_filter_label),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            val sliderPosition = remember(state.ageRange) {
                mutableStateOf(
                    state.ageRange.first.toFloat()..state.ageRange.last.toFloat()
                )
            }
            RangeSlider(
                steps = MAX_AGE_IN_YEARS - MIN_AGE_IN_YEARS + 1,
                value = sliderPosition.value.start..sliderPosition.value.endInclusive,
                valueRange = MIN_AGE_IN_YEARS.toFloat()..MAX_AGE_IN_YEARS.toFloat(),
                onValueChange = { range ->
                    sliderPosition.value = range
                },
                onValueChangeFinished = {
                    onAgeRangeChanged(
                        sliderPosition.value.start.toInt()..sliderPosition.value.endInclusive.toInt()
                    )
                },
            )
        }
        item {
            Text(
                text = stringResource(
                    R.string.age_filter_selected_range,
                    state.ageRange.first,
                    "${state.ageRange.last}${if (state.ageRange.last == MAX_AGE_IN_YEARS) "+" else ""}"
                )
            )
        }

        item {
            Text(
                text = stringResource(R.string.fandom_category_filter_label),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                FandomCategory.entries.forEach { category ->
                    val isSelected = state.selectedCategories.contains(category)
                    Text(
                        text = stringResource(category.toStringId()),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (isSelected) 2.dp else (-1).dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .background(
                                category.getColor().copy(
                                    alpha = if (isSelected) 1f else 0.5f
                                )
                            )
                            .clickable { onCategoryToggled(category) }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.fandom_filter_label),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            FandomInput(
                foundFandoms = state.foundFandoms,
                selectedFandoms = state.selectedFandoms,
                onFandomAdded = onFandomAdded,
                onFandomRemoved = onFandomRemoved,
                onSearch = onFandomSearch,
                areFandomsLoading = state.areFandomsLoading,
            )
        }

        item {
            val userCityText = state.userCity?.getName() ?: stringResource(R.string.city_filter_no_city)
            Text(
                text = stringResource(R.string.city_filter_label, userCityText),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            MyCheckBox(
                isChecked = state.onlyInUserCity,
                onCheckedChange = onLocationToggled,
                label = stringResource(R.string.city_filter_only_in_city_checkbox)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onResetFilters,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.reset_filters_button))
                }
                Button(
                    modifier = Modifier
                        .weight(1f),
                    onClick = onApplyFilters
                ) {
                    Text(stringResource(R.string.apply_filters_button))
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    LoadingBlock()
}

@Composable
private fun IdleState() {
    LoadingBlock()
}