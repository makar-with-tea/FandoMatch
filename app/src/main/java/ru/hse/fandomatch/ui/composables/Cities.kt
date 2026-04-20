package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.utils.getIcon

@Composable
fun CityInput(
    foundCities: List<City>,
    onCitySelected: (City) -> Unit,
    onSearch: (String?) -> Unit,
    areCitiesLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var cityInput by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(foundCities.isNotEmpty()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun clear() {
        cityInput = ""
        showDropdown = false
        onSearch(null)
    }

    LaunchedEffect(foundCities, cityInput) {
        if (foundCities.isNotEmpty() && cityInput.isNotBlank()) {
            showDropdown = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = cityInput,
            onValueChange = { input -> cityInput = input },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    showDropdown = true
                    keyboardController?.hide()
                    onSearch(cityInput)
                }
            ),
            placeholder = { Text(stringResource(R.string.search_city_label)) },
            trailingIcon = {
                if (showDropdown || cityInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            clear()
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.clear_search_icon_description),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showDropdown) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f))
            ) {
                items(foundCities) { city ->
                    Text(
                        text = city.getName(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCitySelected(city)
                                clear()
                            }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (foundCities.isEmpty()) {
                    item {
                        if (areCitiesLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.city_not_found_error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun City.getName(): String {
    val locale = LocalConfiguration.current.locales[0]
    val isRussianLocale = locale.language.equals("ru", ignoreCase = true)
    return if (isRussianLocale) this.nameRussian else this.nameEnglish
}

@Composable
fun CityAndGenderText(
    city: City?,
    gender: Gender,
    modifier: Modifier = Modifier,
    color: Color,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cityText = city?.getName() ?: stringResource(R.string.profile_no_city)
        Text(
            text = "$cityText,",
            color = color,
        )

        val genderIcon = when (gender) {
            Gender.FEMALE, Gender.MALE -> gender.getIcon()
            Gender.NOT_SPECIFIED -> null
        }
        val genderText = when (gender) {
            Gender.FEMALE -> R.string.gender_female_icon_description
            Gender.MALE -> R.string.gender_male_icon_description
            Gender.NOT_SPECIFIED -> R.string.gender_not_specified_icon_description
        }
        genderIcon?.let {
            Icon(
                modifier = Modifier
                    .size(16.dp),
                imageVector = genderIcon,
                tint = color,
                contentDescription = stringResource(genderText),
            )
        } ?: Text(
            text = stringResource(genderText),
            color = color,
        )
    }
}
