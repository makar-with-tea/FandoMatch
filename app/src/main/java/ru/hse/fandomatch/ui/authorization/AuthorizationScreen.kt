package ru.hse.fandomatch.ui.authorization

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyPasswordField
import ru.hse.fandomatch.ui.composables.MyTextField

@Composable
fun AuthorizationScreen(
    navigateToMatches: () -> Unit,
    viewModel: AuthorizationViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("AuthorizationScreen", "State: $state")
    when (action.value) {
        is AuthorizationAction.NavigateToMatches -> {
            navigateToMatches()
            viewModel.obtainEvent(AuthorizationEvent.Clear)
        }

        null -> {}
    }

    when (state.value) {
        is AuthorizationState.Main -> {
            MainState(
                state.value as AuthorizationState.Main,
                onLoginChanged = {
                    viewModel.obtainEvent(AuthorizationEvent.LoginChanged(it))
                },
                onPasswordChanged = {
                    viewModel.obtainEvent(AuthorizationEvent.PasswordChanged(it))
                },
                onLoginClick = {
                    viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
                },
                onShowPasswordClick = {
                    viewModel.obtainEvent(AuthorizationEvent.ShowPasswordButtonClicked)
                }
            )
        }
        is AuthorizationState.Loading -> {
            LoadingState()
        }
    }
}

@Composable
fun MainState(
    state: AuthorizationState.Main,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onShowPasswordClick: () -> Unit
) {
    val login: MutableState<String> = remember { mutableStateOf(state.login) }
    val password: MutableState<String> = remember { mutableStateOf(state.password) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            MyTextField(
                value = login.value,
                label = stringResource(id = R.string.login_label),
                isError = state.loginError != AuthorizationState.AuthorizationError.IDLE,
                errorText = if (state.loginError != AuthorizationState.AuthorizationError.NETWORK)
                    state.loginError.toText() else null
            ) {
                onLoginChanged(it)
                login.value = it
            }
            MyPasswordField(
                value = password.value,
                label = stringResource(id = R.string.password_label),
                isError = state.passwordError != AuthorizationState.AuthorizationError.IDLE,
                onValueChange = {
                    onPasswordChanged(it)
                    password.value = it
                                },
                onIconClick = {
                    onShowPasswordClick()
                },
                passwordVisibility = state.passwordVisibility,
                errorText = state.passwordError.toText()
            )
            Button(
                enabled = !state.passwordError.isBlocking() && !state.loginError.isBlocking(),
                onClick = { onLoginClick() }
            ) {
                Text(stringResource(id = R.string.login_button))
            }
        }
    }

    if (state.isLoading) {
        LoadingBlock()
    }
}

@Composable
private fun AuthorizationState.AuthorizationError.toText() = when (this) {
    AuthorizationState.AuthorizationError.EMPTY_LOGIN ->
        stringResource(id = R.string.empty_login_error)
    AuthorizationState.AuthorizationError.EMPTY_PASSWORD ->
        stringResource(id = R.string.empty_password_error)
    AuthorizationState.AuthorizationError.INVALID_CREDENTIALS ->
        stringResource(id = R.string.invalid_credentials_error)
    AuthorizationState.AuthorizationError.NETWORK ->
        stringResource(id = R.string.network_error)
    AuthorizationState.AuthorizationError.IDLE -> ""
}

@Composable
fun LoadingState() {
    LoadingBlock()
}

@Preview(showBackground = true)
@Composable
fun AuthorizationScreenPreview() {
    AuthorizationScreen(
            {}
    )
}
