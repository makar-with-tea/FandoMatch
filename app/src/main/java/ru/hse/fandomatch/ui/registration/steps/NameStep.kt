package ru.hse.fandomatch.ui.registration.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.MyTextField
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.registration.RegistrationState
import ru.hse.fandomatch.ui.registration.getText
import ru.hse.fandomatch.ui.registration.isFieldError


@Composable
internal fun NameStep(
    state: RegistrationState.Name,
    onNext: (String, String, String) -> Unit,
) {
    val name = remember { mutableStateOf(state.name) }
    val email = remember { mutableStateOf(state.email) }
    val login = remember { mutableStateOf(state.login) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.registration_title))

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name.value,
                label = stringResource(R.string.name_label),
                isError = state.nameError.isFieldError(),
                errorText = state.nameError.getText()
            ) { name.value = it }
            MyTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email.value,
                label = stringResource(R.string.email_label),
                isError = state.emailError.isFieldError(),
                errorText = state.emailError.getText()
            ) { email.value = it }
            MyTextField(
                modifier = Modifier.fillMaxWidth(),
                value = login.value,
                label = stringResource(R.string.login_label),
                isError = state.loginError.isFieldError(),
                errorText = state.loginError.getText()
            ) { login.value = it }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { onNext(name.value, email.value, login.value) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}
