package ru.hse.fandomatch.ui.passwordrecovery

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyPasswordField
import ru.hse.fandomatch.ui.composables.MyTextField

@Composable
fun PasswordRecoveryScreen(
    navigateToAuthorization: () -> Unit,
    viewModel: PasswordRecoveryViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("PasswordRecoveryScreen", "State: $state")
    when (action.value) {
        is PasswordRecoveryAction.NavigateToAuthorization -> {
            Toast.makeText(
                LocalContext.current,
                stringResource(id = R.string.password_recovery_success),
                Toast.LENGTH_SHORT
            ).show()
            navigateToAuthorization()
            viewModel.obtainEvent(PasswordRecoveryEvent.Clear)
        }

        null -> {}
    }

    when (state.value) {
        is PasswordRecoveryState.Email -> {
            EmailState(
                state = state.value as PasswordRecoveryState.Email,
                onEmailChanged = { viewModel.obtainEvent(PasswordRecoveryEvent.EmailChanged(it)) },
                onSendCodeClick = { viewModel.obtainEvent(PasswordRecoveryEvent.SendCodeClicked) },
            )
        }
        is PasswordRecoveryState.Main -> {
            MainState(
                state = state.value as PasswordRecoveryState.Main,
                onNewPasswordChanged = { viewModel.obtainEvent(PasswordRecoveryEvent.NewPasswordChanged(it)) },
                onRepeatPasswordChanged = {
                    viewModel.obtainEvent(PasswordRecoveryEvent.RepeatNewPasswordChanged(it))
                },
                onToggleNewPasswordVisibility = {
                    viewModel.obtainEvent(PasswordRecoveryEvent.ToggleNewPasswordVisibility)
                },
                onToggleRepeatPasswordVisibility = {
                    viewModel.obtainEvent(PasswordRecoveryEvent.ToggleRepeatNewPasswordVisibility)
                },
                onSaveClick = { code -> viewModel.obtainEvent(PasswordRecoveryEvent.SavePasswordClicked(code)) },
            )
        }
    }
}

@Composable
fun EmailState(
    state: PasswordRecoveryState.Email,
    onEmailChanged: (String) -> Unit,
    onSendCodeClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MyTextField(
            value = state.email,
            label = stringResource(id = R.string.email_label),
            isError = state.emailError != PasswordRecoveryState.PasswordRecoveryError.IDLE,
            errorText = state.emailError.toText(),
            onValueChange = onEmailChanged,
            keyboardType = KeyboardType.Email,
        )

        Button(
            onClick = onSendCodeClick,
            enabled = state.emailError.isButtonAvailable(),
        ) {
            Text(stringResource(id = R.string.send_recovery_code_button))
        }
    }
}

@Composable
fun MainState(
    state: PasswordRecoveryState.Main,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onToggleNewPasswordVisibility: () -> Unit,
    onToggleRepeatPasswordVisibility: () -> Unit,
    onSaveClick: (String) -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            var code by remember { mutableStateOf("") }

            MyTextField(
                value = code,
                label = stringResource(id = R.string.verification_code_label),
                isError = state.codeError != PasswordRecoveryState.PasswordRecoveryError.IDLE,
                errorText = state.codeError.toText(),
                onValueChange = { code = it.filter { ch -> ch.isDigit() } },
                keyboardType = KeyboardType.NumberPassword,
            )

            MyPasswordField(
                value = state.newPassword,
                label = stringResource(id = R.string.new_password_label),
                isError = state.newPasswordError != PasswordRecoveryState.PasswordRecoveryError.IDLE,
                onValueChange = onNewPasswordChanged,
                onIconClick = onToggleNewPasswordVisibility,
                passwordVisibility = state.newPasswordVisibility,
                errorText = state.newPasswordError.toText(),
            )

            MyPasswordField(
                value = state.repeatNewPassword,
                label = stringResource(id = R.string.repeat_new_password_label),
                isError = state.repeatNewPasswordError != PasswordRecoveryState.PasswordRecoveryError.IDLE,
                onValueChange = onRepeatPasswordChanged,
                onIconClick = onToggleRepeatPasswordVisibility,
                passwordVisibility = state.repeatNewPasswordVisibility,
                errorText = state.repeatNewPasswordError.toText(),
            )

            Button(
                onClick = { onSaveClick(code) },
                enabled = !state.isLoading && state.codeError.isButtonAvailable()
                        && state.newPasswordError.isButtonAvailable()
                        && state.repeatNewPasswordError.isButtonAvailable(),
            ) {
                Text(stringResource(id = R.string.save_button))
            }
        }
    }

    if (state.isLoading) {
        LoadingBlock()
    }
}

@Composable
private fun PasswordRecoveryState.PasswordRecoveryError.toText() = when (this) {
    PasswordRecoveryState.PasswordRecoveryError.EMPTY_CODE ->
        stringResource(id = R.string.empty_code_error)
    PasswordRecoveryState.PasswordRecoveryError.INVALID_CODE ->
        stringResource(id = R.string.invalid_code_error)
    PasswordRecoveryState.PasswordRecoveryError.PASSWORD_LENGTH ->
        stringResource(id = R.string.password_length_error)
    PasswordRecoveryState.PasswordRecoveryError.PASSWORD_CONTENT ->
        stringResource(id = R.string.password_content_error)
    PasswordRecoveryState.PasswordRecoveryError.PASSWORD_MISMATCH ->
        stringResource(id = R.string.password_mismatch_error)
    PasswordRecoveryState.PasswordRecoveryError.NETWORK ->
        stringResource(id = R.string.network_error)
    PasswordRecoveryState.PasswordRecoveryError.EMAIL_CONTENT ->
        stringResource(id = R.string.email_content_error)
    PasswordRecoveryState.PasswordRecoveryError.IDLE -> ""
}

@Preview(showBackground = true)
@Composable
fun PasswordRecoveryScreenPreview() {
    PasswordRecoveryScreen(
        navigateToAuthorization = {},
    )
}
