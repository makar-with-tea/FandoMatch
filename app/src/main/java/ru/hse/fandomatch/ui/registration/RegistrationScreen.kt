package ru.hse.fandomatch.ui.registration

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.registration.steps.AvatarStep
import ru.hse.fandomatch.ui.registration.steps.CodeStep
import ru.hse.fandomatch.ui.registration.steps.DateStep
import ru.hse.fandomatch.ui.registration.steps.GenderStep
import ru.hse.fandomatch.ui.registration.steps.NameStep
import ru.hse.fandomatch.ui.registration.steps.PasswordStep

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RegistrationScreen(
    navigateToMatches: () -> Unit,
    navigateBack: () -> Unit,
    setTopBarState: (TopBarState?) -> Unit,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()
    Log.i("RegistrationScreen", "State: ${state.value}")

    setTopBarState(
        if (state.value != RegistrationState.Idle && state.value != RegistrationState.Loading) {
            Log.i("RegistrationScreen", "Setting top bar state for registration")
            TopBarState(
                titleContent = {
                    LinearWavyProgressIndicator(
                        progress = {
                            when (state.value) {
                                RegistrationState.Idle,
                                RegistrationState.Loading -> 0f

                                is RegistrationState.Name -> 0.167f
                                is RegistrationState.Code -> 0.333f
                                is RegistrationState.DateOfBirth -> 0.5f
                                is RegistrationState.GenderChoice -> 0.667f
                                is RegistrationState.Avatar -> 0.833f
                                is RegistrationState.Password -> 1f
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            )
        } else null.also {
            Log.i("RegistrationScreen", "Clearing top bar state for registration")
        }
    )

    when (action.value) {
        is RegistrationAction.NavigateToMatches -> {
            navigateToMatches()
            viewModel.obtainEvent(RegistrationEvent.Clear)
        }

        is RegistrationAction.NavigateBack -> {
            navigateBack()
            viewModel.obtainEvent(RegistrationEvent.Clear)
        }

        null -> {}
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = state.value) {
            RegistrationState.Idle -> LoadingBlock()
            RegistrationState.Loading -> LoadingBlock()
            is RegistrationState.Name -> NameStep(
                state = state,
                onNameChanged = { viewModel.obtainEvent(RegistrationEvent.NameChanged(it)) },
                onEmailChanged = { viewModel.obtainEvent(RegistrationEvent.EmailChanged(it)) },
                onLoginChanged = { viewModel.obtainEvent(RegistrationEvent.LoginChanged(it)) },
                onNext = { viewModel.obtainEvent(RegistrationEvent.NameSubmitted) },
            )

            is RegistrationState.Code -> CodeStep(
                state = state,
                onNext = { code -> viewModel.obtainEvent(RegistrationEvent.CodeSubmitted(code)) },
                onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) },
            )

            is RegistrationState.DateOfBirth -> DateStep(
                state = state,
                onNext = { dobMillis ->
                    viewModel.obtainEvent(
                        RegistrationEvent.DateSelected(
                            dobMillis
                        )
                    )
                },
                onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) },
            )

            is RegistrationState.GenderChoice -> GenderStep(
                state = state,
                onNext = { gender -> viewModel.obtainEvent(RegistrationEvent.GenderSelected(gender)) },
                onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) },
            )

            is RegistrationState.Avatar -> AvatarStep(
                state = state,
                onNext = { avatarByteArray ->
                    viewModel.obtainEvent(
                        RegistrationEvent.AvatarSelected(
                            avatarByteArray
                        )
                    )
                },
                onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) },
            )

            is RegistrationState.Password -> PasswordStep(
                state = state,
                onPasswordChanged = { viewModel.obtainEvent(RegistrationEvent.PasswordChanged(it)) },
                onPasswordRepeatChanged = {
                    viewModel.obtainEvent(
                        RegistrationEvent.PasswordRepeatChanged(
                            it
                        )
                    )
                },
                onAgreedToTermsChanged = {
                    viewModel.obtainEvent(
                        RegistrationEvent.AgreedToTermsChanged(
                            it
                        )
                    )
                },
                onCompleteRegistration = { viewModel.obtainEvent(RegistrationEvent.PasswordSubmit) },
                onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) },
                onPasswordVisibilityChanged = { viewModel.obtainEvent(RegistrationEvent.PasswordVisibilityChanged) },
                onPasswordRepeatVisibilityChanged = { viewModel.obtainEvent(RegistrationEvent.PasswordRepeatVisibilityChanged) },
            )
        }
    }
}

@Composable
internal fun RegistrationState.RegistrationError.getText(): String {
    return when (this) {
        RegistrationState.RegistrationError.NAME_LENGTH -> stringResource(R.string.name_length_error)
        RegistrationState.RegistrationError.NAME_CONTENT -> stringResource(R.string.name_content_error)
        RegistrationState.RegistrationError.LOGIN_LENGTH -> stringResource(R.string.login_length_error)
        RegistrationState.RegistrationError.LOGIN_CONTENT -> stringResource(R.string.login_content_error)
        RegistrationState.RegistrationError.PASSWORD_LENGTH -> stringResource(R.string.password_length_error)
        RegistrationState.RegistrationError.PASSWORD_CONTENT -> stringResource(R.string.password_content_error)
        RegistrationState.RegistrationError.PASSWORD_MISMATCH -> stringResource(R.string.password_mismatch_error)
        RegistrationState.RegistrationError.EMAIL_CONTENT -> stringResource(R.string.email_content_error)
        RegistrationState.RegistrationError.LOGIN_TAKEN -> stringResource(R.string.login_taken_error)
        RegistrationState.RegistrationError.NETWORK -> stringResource(R.string.network_error)
        RegistrationState.RegistrationError.DOB_TOO_YOUNG -> stringResource(R.string.birthdate_too_young_error)
        RegistrationState.RegistrationError.DOB_EMPTY -> stringResource(R.string.birthdate_empty_error)
        RegistrationState.RegistrationError.GENDER_NOT_SELECTED -> stringResource(R.string.gender_not_selected_error)
        RegistrationState.RegistrationError.INVALID_CODE -> stringResource(R.string.invalid_code_error)
        RegistrationState.RegistrationError.IDLE -> ""
    }
}

internal fun RegistrationState.RegistrationError.isFieldError(): Boolean {
    return when (this) {
        RegistrationState.RegistrationError.IDLE,
        RegistrationState.RegistrationError.LOGIN_TAKEN,
        RegistrationState.RegistrationError.NETWORK -> false
        else -> true
    }
}
