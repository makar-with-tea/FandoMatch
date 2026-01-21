package ru.hse.fandomatch.ui.registration

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
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.registration.steps.AvatarStep
import ru.hse.fandomatch.ui.registration.steps.DateStep
import ru.hse.fandomatch.ui.registration.steps.GenderStep
import ru.hse.fandomatch.ui.registration.steps.NameStep
import ru.hse.fandomatch.ui.registration.steps.PasswordStep

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RegistrationScreen(
    navigateToMatches: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    Scaffold {
        val state = viewModel.state.collectAsState()
        val action = viewModel.action.collectAsState()

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
            if (state.value != RegistrationState.Idle && state.value != RegistrationState.Loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            viewModel.obtainEvent(RegistrationEvent.Back)
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = stringResource(R.string.arrow_back_description),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    LinearWavyProgressIndicator(
                        progress = {
                            when (state.value) {
                                RegistrationState.Idle,
                                RegistrationState.Loading -> 0f

                                is RegistrationState.Name -> 0.2f
                                is RegistrationState.DateOfBirth -> 0.4f
                                is RegistrationState.GenderChoice -> 0.6f
                                is RegistrationState.Avatar -> 0.8f
                                is RegistrationState.Password -> 1f
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            when (val state = state.value) {
                RegistrationState.Idle -> LoadingBlock()
                RegistrationState.Loading -> LoadingBlock()
                is RegistrationState.Main -> MainState(
                    state = state,
                    saveName = { name, login, email ->
                            viewModel.obtainEvent(
                                RegistrationEvent.NameSubmitted(
                                    name,
                                    login,
                                    email,
                                )
                            )
                    },
                    saveDateOfBirth = { dobMillis ->
                        viewModel.obtainEvent(RegistrationEvent.DateSelected(dobMillis))
                    },
                    saveGender = { gender ->
                        viewModel.obtainEvent(RegistrationEvent.GenderSelected(gender))
                    },
                    saveAvatar = { avatarByteArray ->
                        viewModel.obtainEvent(
                            RegistrationEvent.AvatarSelected(
                                avatarByteArray
                            )
                        )
                    },
                    savePassword = { password, passwordRepeat, agreedToTerms ->
                        viewModel.obtainEvent(
                            RegistrationEvent.PasswordSubmit(
                                password,
                                passwordRepeat,
                                agreedToTerms,
                            )
                        )
                    },
                    onBackPressed = {
                        viewModel.obtainEvent(RegistrationEvent.Back)
                    },
                    onPasswordVisibilityChanged = {
                        viewModel.obtainEvent(RegistrationEvent.PasswordVisibilityChanged)
                    },
                    onPasswordRepeatVisibilityChanged = {
                        viewModel.obtainEvent(RegistrationEvent.PasswordRepeatVisibilityChanged)
                    }
                )
            }
        }
    }
}

@Composable
private fun MainState(
    state: RegistrationState.Main,
    saveName: (String, String, String) -> Unit,
    saveDateOfBirth: (Long?) -> Unit,
    saveGender: (Gender) -> Unit,
    saveAvatar: (ByteArray?) -> Unit,
    savePassword: (String, String, Boolean) -> Unit,
    onBackPressed: () -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onPasswordRepeatVisibilityChanged: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is RegistrationState.Name -> {
                    NameStep(
                        state = state,
                        onNext = saveName,
                    )
                }

                is RegistrationState.DateOfBirth -> {
                    DateStep(
                        state = state,
                        onNext = saveDateOfBirth,
                        onBackPressed = onBackPressed,
                    )
                }

                is RegistrationState.GenderChoice -> GenderStep(
                    state = state,
                    onNext = saveGender,
                    onBackPressed = onBackPressed,
                )

                is RegistrationState.Avatar -> AvatarStep(
                    state = state,
                    onNext = saveAvatar,
                    onBackPressed = onBackPressed,
                )

                is RegistrationState.Password -> PasswordStep(
                    state = state,
                    onCompleteRegistration = savePassword,
                    onBackPressed = onBackPressed,
                    onPasswordVisibilityChanged = onPasswordVisibilityChanged,
                    onPasswordRepeatVisibilityChanged = onPasswordRepeatVisibilityChanged,
                )
            }
        }
    }
}

@Composable
internal fun RegistrationState.RegistrationError.getText(): String {
    return when (this) {
        RegistrationState.RegistrationError.NAME_LENGTH -> stringResource(R.string.name_length_error)
        RegistrationState.RegistrationError.NAME_CONTENT -> stringResource(R.string.name_content_error)
        RegistrationState.RegistrationError.SURNAME_LENGTH -> stringResource(R.string.surname_length_error)
        RegistrationState.RegistrationError.SURNAME_CONTENT -> stringResource(R.string.surname_content_error)
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
        RegistrationState.RegistrationError.IDLE -> ""
    }
}

internal fun RegistrationState.RegistrationError.isFieldError(): Boolean {
    return when (this) {
        RegistrationState.RegistrationError.IDLE,
        RegistrationState.RegistrationError.NETWORK -> false
        else -> true
    }
}
