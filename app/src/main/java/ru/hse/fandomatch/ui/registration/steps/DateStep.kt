package ru.hse.fandomatch.ui.registration.steps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.registration.RegistrationState
import ru.hse.fandomatch.ui.registration.getText
import ru.hse.fandomatch.ui.registration.isFieldError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateStep(
    state: RegistrationState.DateOfBirth,
    onNext: (Long?) -> Unit,
    onBackPressed: () -> Unit,
) {
    BackHandler {
        onBackPressed()
    }
    val datePickerState = rememberDatePickerState(state.dateOfBirthMillis)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(
            text = stringResource(R.string.birthdate_title),
        )

        DatePicker(
            state = datePickerState,
            showModeToggle = true,
        )

        if (state.error.isFieldError()) {
            // todo обновление при смене даты а не нажатии на "Далее"
            Text(
                text = state.error.getText(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNext(datePickerState.selectedDateMillis) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}
