package ru.hse.fandomatch.ui.filters

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.getColor
import ru.hse.fandomatch.getName
import ru.hse.fandomatch.stringId
import ru.hse.fandomatch.toStringId
import ru.hse.fandomatch.ui.composables.BasicErrorState
import ru.hse.fandomatch.ui.composables.FandomInput
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MySwitch
import ru.hse.fandomatch.ui.theme.FandoMatchTheme
import kotlin.math.roundToInt

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

        is FiltersAction.ShowErrorToast -> {
            Toast.makeText(
                LocalContext.current,
                stringResource(R.string.filters_error_toast),
                Toast.LENGTH_SHORT
            ).show()
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
        is FiltersState.Error -> {
            ErrorState(
                onRetry = { viewModel.obtainEvent(FiltersEvent.LoadInitialFilters) }
            )
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
    Column(
        modifier = Modifier
            .fillMaxSize(),
        ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.gender_title),
                        fontSize = 18.sp,
                    )

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
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.age_filter_label),
                        fontSize = 18.sp,
                    )

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
                            val roundedStart = range.start.roundToInt()
                            val roundedEnd = range.endInclusive.roundToInt()
                            sliderPosition.value = roundedStart.toFloat()..roundedEnd.toFloat()
                        },
                        onValueChangeFinished = {
                            onAgeRangeChanged(
                                sliderPosition.value.start.toInt()..sliderPosition.value.endInclusive.toInt()
                            )
                        },
                    )

                    Text(
                        text = stringResource(
                            R.string.age_filter_selected_range,
                            state.ageRange.first,
                            "${state.ageRange.last}${if (state.ageRange.last == MAX_AGE_IN_YEARS) "+" else ""}"
                        )
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.fandom_category_filter_label),
                        fontSize = 18.sp,
                    )

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
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.fandom_filter_label),
                        fontSize = 18.sp,
                    )

                    FandomInput(
                        foundFandoms = state.foundFandoms,
                        selectedFandoms = state.selectedFandoms,
                        onFandomAdded = onFandomAdded,
                        onFandomRemoved = onFandomRemoved,
                        onSearch = onFandomSearch,
                        areFandomsLoading = state.areFandomsLoading,
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val userCityText =
                        state.userCity?.getName() ?: stringResource(R.string.city_filter_no_city)
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.city_filter_label, userCityText),
                        fontSize = 18.sp,
                    )
                    MySwitch(
                        isChecked = state.onlyInUserCity,
                        onCheckedChange = onLocationToggled,
                        label = stringResource(R.string.city_filter_only_in_city_checkbox)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onResetFilters,
                colors = ButtonDefaults.buttonColors(
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

@Composable
private fun LoadingState() {
    LoadingBlock()
}

@Composable
private fun IdleState() {
    LoadingBlock()
}

@Composable
private fun ErrorState(
    onRetry: () -> Unit,
) {
    BasicErrorState(onRetry)
}

@Preview(showBackground = true)
@Composable
fun MainStatePreview() {
    FandoMatchTheme {
        MainState(
            state = FiltersState.Main(
                selectedGenders = listOf(Gender.FEMALE),
                ageRange = 18..30,
                selectedCategories = listOf(FandomCategory.ANIME_MANGA, FandomCategory.BOOKS),
                selectedFandoms = listOf(
                    Fandom(id = "1", name = "Fandom 1", category = FandomCategory.ANIME_MANGA),
                    Fandom(id = "2", name = "Fandom 2", category = FandomCategory.CARTOONS)
                ),
                foundFandoms = listOf(),
                onlyInUserCity = true,
                userCity = City(nameRussian = "Москва", nameEnglish = "Moscow"),
                areFandomsLoading = false
            ),
            onGenderSelected = {},
            onAgeRangeChanged = {},
            onCategoryToggled = {},
            onFandomAdded = {},
            onFandomRemoved = {},
            onFandomSearch = {},
            onLocationToggled = {},
            onResetFilters = {},
            onApplyFilters = {}
        )
    }
}
