package ru.hse.fandomatch.ui.registration.steps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.MyCheckBox
import ru.hse.fandomatch.ui.composables.MyPasswordField
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.registration.RegistrationState
import ru.hse.fandomatch.ui.registration.getText
import ru.hse.fandomatch.ui.registration.isFieldError

@Composable
internal fun PasswordStep(
    state: RegistrationState.Password,
    onPasswordChanged: (String) -> Unit,
    onPasswordRepeatChanged: (String) -> Unit,
    onAgreedToTermsChanged: (Boolean) -> Unit,
    onCompleteRegistration: () -> Unit,
    onBackPressed: () -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onPasswordRepeatVisibilityChanged: () -> Unit,
) {
    BackHandler {
        onBackPressed()
    }

    val password = remember { mutableStateOf(state.password) }
    val repeat = remember { mutableStateOf(state.passwordRepeat) }
    val agreed = remember { mutableStateOf(state.agreedToTerms) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.password_label))

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyPasswordField(
                modifier = Modifier.fillMaxWidth(),
                value = password.value,
                label = stringResource(R.string.password_label),
                isError = state.passwordError.isFieldError(),
                errorText = state.passwordError.getText(),
                onValueChange = {
                    onPasswordChanged(it)
                    password.value = it
                },
                onIconClick = onPasswordVisibilityChanged,
                passwordVisibility = state.passwordVisibility
            )
            MyPasswordField(
                modifier = Modifier.fillMaxWidth(),
                value = repeat.value,
                label = stringResource(R.string.repeat_password_label),
                isError = state.passwordRepeatError.isFieldError(),
                errorText = state.passwordRepeatError.getText(),
                onValueChange = {
                    onPasswordRepeatChanged(it)
                    repeat.value = it
                },
                onIconClick = onPasswordRepeatVisibilityChanged,
                passwordVisibility = state.passwordRepeatVisibility
            )
            MyCheckBox(
                isChecked = agreed.value,
                onCheckedChange = {
                    onAgreedToTermsChanged(it)
                    agreed.value = it
                },
                label = stringResource(R.string.agree_terms)
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onCompleteRegistration() },
            enabled = agreed.value && !state.passwordError.isFieldError() && !state.passwordRepeatError.isFieldError()
        ) { Text(stringResource(R.string.complete_registration)) }
    }
}
