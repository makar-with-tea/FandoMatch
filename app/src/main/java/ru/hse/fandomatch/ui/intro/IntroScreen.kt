package ru.hse.fandomatch.ui.intro

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyTitle

@Composable
fun IntroScreen(
    navigateToMatches: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToRegistration: () -> Unit,
    viewModel: IntroViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("IntroScreen", "State: $state")
    when (action.value) {
        is IntroAction.NavigateToMatches -> {
            navigateToMatches()
            viewModel.obtainEvent(IntroEvent.Clear)
        }

        is IntroAction.NavigateToRegistration -> {
            navigateToRegistration()
            viewModel.obtainEvent(IntroEvent.Clear)
        }

        is IntroAction.NavigateToLogin -> {
            navigateToLogin()
            viewModel.obtainEvent(IntroEvent.Clear)
        }

        null -> {}
    }

    when (state.value) {
        is IntroState.Main -> {
            MainState(
                onLoginClick = {
                    viewModel.obtainEvent(IntroEvent.GoToLoginButtonClicked)
                },
                onRegistrationClick = {
                    viewModel.obtainEvent(IntroEvent.GoToRegistrationButtonClicked)
                }
            )
        }

        IntroState.Loading -> {
            LoadingState()
        }

        IntroState.Idle -> {
            IdleState()
            viewModel.obtainEvent(IntroEvent.CheckPastLogin)
        }
    }
}

@Composable
fun MainState(
    onLoginClick: () -> Unit,
    onRegistrationClick: () -> Unit,
) {
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
            MyTitle(stringResource(id = R.string.intro_title))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                onRegistrationClick()
            }) {
                Text(stringResource(id = R.string.register_button))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                onLoginClick()
            }) {
                Text(stringResource(id = R.string.login_button))
            }
        }
    }
}

@Composable
fun LoadingState() {
    LoadingBlock()
}

@Composable
fun IdleState() {
    LoadingBlock()
}

@Preview(showBackground = true)
@Composable
fun IntroScreenPreview() {
    IntroScreen(
        {}, {}, {}
    )
}
