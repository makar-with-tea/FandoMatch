package ru.hse.fandomatch.ui.registration.steps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.utils.stringId
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.registration.RegistrationState
import ru.hse.fandomatch.ui.registration.getText
import ru.hse.fandomatch.ui.registration.isFieldError
import ru.hse.fandomatch.utils.getIcon

@Composable
internal fun GenderStep(
    state: RegistrationState.GenderChoice,
    onNext: (Gender) -> Unit,
    onBackPressed: () -> Unit,
) {
    BackHandler {
        onBackPressed()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.gender_title))

        var selectedGender by remember { mutableStateOf(state.gender) }
        val options = listOf(
            Gender.MALE,
            Gender.FEMALE,
            Gender.NOT_SPECIFIED,
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { index, gender ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = { selectedGender = gender },
                        selected = selectedGender == gender,
                        label = { Text(stringResource(gender.stringId())) },
                        icon = {
                            if (selectedGender == gender) {
                                Icon(
                                    imageVector = gender.getIcon(),
                                    contentDescription = null,
                                )
                            }
                        }
                    )
                }
            }

            if (state.error.isFieldError()) {
                Text(
                    state.error.getText(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNext(selectedGender) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}
