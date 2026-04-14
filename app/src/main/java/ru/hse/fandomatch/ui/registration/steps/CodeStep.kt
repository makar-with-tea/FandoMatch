package ru.hse.fandomatch.ui.registration.steps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.MyTextField
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.registration.RegistrationState
import ru.hse.fandomatch.ui.registration.getText

@Composable
fun CodeStep(
    state: RegistrationState.Code,
    onNext: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
    BackHandler {
        onBackPressed()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var code by remember { mutableStateOf("") }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyTitle(
                text = stringResource(id = R.string.code_verification_title)
            )
            Text(
                text = stringResource(id = R.string.code_verification_description),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        MyTextField(
            value = code,
            label = stringResource(id = R.string.verification_code_label),
            isError = state.codeError != RegistrationState.RegistrationError.IDLE,
            errorText = state.codeError.getText(),
            onValueChange = { code = it.filter { ch -> ch.isDigit() } },
            keyboardType = KeyboardType.NumberPassword,
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { onNext(code) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}
