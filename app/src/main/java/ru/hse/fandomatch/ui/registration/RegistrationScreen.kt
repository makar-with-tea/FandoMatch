package ru.hse.fandomatch.ui.registration

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.DatePickerDocked
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyPasswordField
import ru.hse.fandomatch.ui.composables.MyTextField
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.navigation.TopBar
import ru.hse.fandomatch.ui.navigation.TopBarState
import java.time.Instant

@Composable
fun RegistrationScreen(
    navigateToMatches: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel()
) {
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

    if (state.value != RegistrationState.Idle && state.value != RegistrationState.Loading) {
        TopBar(
            state = TopBarState.ProgressBar(
                progress = when (state.value) {
                    RegistrationState.Idle,
                    RegistrationState.Loading -> 0f

                    is RegistrationState.Name -> 0.2f
                    is RegistrationState.DateOfBirth -> 0.4f
                    is RegistrationState.Gender -> 0.6f
                    is RegistrationState.Avatar -> 0.8f
                    is RegistrationState.Password -> 1f
                }
            ),
            onBackClick = { viewModel.obtainEvent(RegistrationEvent.Back) }
        )
    }
    when (val state = state.value) {
        RegistrationState.Idle -> LoadingBlock()
        RegistrationState.Loading -> LoadingBlock()
        is RegistrationState.Name -> {
            NameStep(
                state = state,
                onNext = { name, login, email ->
                    viewModel.obtainEvent(
                        RegistrationEvent.NameSubmitted(
                            name,
                            login,
                            email,
                        )
                    )
                },
            )
        }
        is RegistrationState.DateOfBirth -> {
            DateStep(
                state = state,
                onNext = { dobMillis ->
                    viewModel.obtainEvent(RegistrationEvent.DateSelected(dobMillis))
                },
                onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) }
            )
        }
        is RegistrationState.Gender -> GenderStep(
            state = state,
            onNext = { gender ->
                viewModel.obtainEvent(RegistrationEvent.GenderSelected(gender))
            },
            onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) }
        )
        is RegistrationState.Avatar -> AvatarStep(
            state = state,
            onNext = { avatarUri ->
                viewModel.obtainEvent(RegistrationEvent.AvatarSelected(avatarUri))
            },
            onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) }
        )
        is RegistrationState.Password -> PasswordStep(
            state = state,
            onCompleteRegistration = { password, passwordRepeat, agreedToTerms ->
                viewModel.obtainEvent(
                    RegistrationEvent.PasswordSubmit(
                        password,
                        passwordRepeat,
                        agreedToTerms,
                    )
                )
            },
            onBackPressed = { viewModel.obtainEvent(RegistrationEvent.Back) },
            onPasswordVisibilityChanged = {
                viewModel.obtainEvent(RegistrationEvent.PasswordVisibilityChanged)
            },
            onPasswordRepeatVisibilityChanged = {
                viewModel.obtainEvent(RegistrationEvent.PasswordRepeatVisibilityChanged)
            }
        )
    }
}

@Composable
private fun NameStep(
    state: RegistrationState.Name,
    onNext: (String, String, String) -> Unit,
) {
    val name = remember { mutableStateOf(state.name) }
    val email = remember { mutableStateOf(state.email) }
    val login = remember { mutableStateOf(state.login) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.registration_title))
        MyTextField(
            value = name.value,
            label = stringResource(R.string.name_label),
            isError = state.nameError.isFieldError(),
            errorText = state.nameError.getText()
        ) { name.value = it }
        MyTextField(
            value = email.value,
            label = stringResource(R.string.email_label),
            isError = state.emailError.isFieldError(),
            errorText = state.emailError.getText()
        ) { email.value = it }
        MyTextField(
            value = login.value,
            label = stringResource(R.string.login_label),
            isError = state.loginError.isFieldError(),
            errorText = state.loginError.getText()
        ) { login.value = it }
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = !state.isLoading,
            onClick = { onNext(name.value, email.value, login.value) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateStep(
    state: RegistrationState.DateOfBirth,
    onNext: (Long?) -> Unit,
    onBackPressed: () -> Unit,
) {
    BackHandler {
        onBackPressed()
    }
    val datePickerState = rememberDatePickerState(state.dateOfBirthMillis)
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.birthdate_title))
        Text(
            text = datePickerState.selectedDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    .toString()
            } ?: stringResource(R.string.select_birthdate)
        )

        DatePickerDocked(datePickerState)

        Spacer(Modifier.height(12.dp))
        if (state.error.isFieldError()) {
            Text(
                text = state.error.getText(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = state.error == RegistrationState.RegistrationError.IDLE,
            onClick = { onNext(datePickerState.selectedDateMillis) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}

@Composable
private fun GenderStep(
    state: RegistrationState.Gender,
    onNext: (GenderType) -> Unit,
    onBackPressed: () -> Unit,
) {
    BackHandler {
        onBackPressed()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.gender_title))

        var selectedIndex by remember { mutableIntStateOf(0) }
        val options = listOf(
            GenderType.MALE,
            GenderType.FEMALE,
            GenderType.UNSPECIFIED,
            )

        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex,
                    label = { Text(stringResource(label.stringId())) }
                )
            }
        }

        if (state.error.isFieldError()) {
            Text(state.error.getText(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onNext(options[selectedIndex]) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}

@Composable
private fun AvatarStep(
    state: RegistrationState.Avatar,
    onNext: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
    BackHandler {
        onBackPressed()
    }

    val uri = remember { mutableStateOf(state.avatarUri.orEmpty()) }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.avatar_title))
        Text(text = state.avatarUri ?: stringResource(R.string.no_avatar_selected))
        Spacer(Modifier.height(8.dp))

        MyTextField(
            value = uri.value,
            label = stringResource(R.string.name_label),
            isError = false,
        ) { uri.value = it }


        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { onNext(uri.value) }
        ) { Text(stringResource(R.string.next_step)) }
    }
}

@Composable
private fun PasswordStep(
    state: RegistrationState.Password,
    onCompleteRegistration: (String, String, Boolean) -> Unit,
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyTitle(stringResource(R.string.password_label))
        MyPasswordField(
            value = password.value,
            label = stringResource(R.string.password_label),
            isError = state.passwordError.isFieldError(),
            errorText = state.passwordError.getText(),
            onValueChange = { password.value = it },
            onIconClick = onPasswordVisibilityChanged,
            passwordVisibility = state.passwordVisibility
        )
        MyPasswordField(
            value = repeat.value,
            label = stringResource(R.string.repeat_password_label),
            isError = state.passwordRepeatError.isFieldError(),
            errorText = state.passwordRepeatError.getText(),
            onValueChange = { repeat.value = it },
            onIconClick = onPasswordRepeatVisibilityChanged,
            passwordVisibility = state.passwordRepeatVisibility
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                agreed.value = !agreed.value
            }
        ) {
            Checkbox(checked = agreed.value, onCheckedChange = { agreed.value = it })
            Text(stringResource(R.string.agree_terms))
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onCompleteRegistration(password.value, repeat.value, agreed.value) },
            enabled = agreed.value,
        ) { Text(stringResource(R.string.complete_registration)) }
    }
}

@Composable
private fun RegistrationState.RegistrationError.getText(): String {
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

private fun GenderType.stringId(): Int = when (this) {
    GenderType.MALE -> R.string.male_gender
    GenderType.FEMALE -> R.string.female_gender
    GenderType.UNSPECIFIED -> R.string.unspecified_gender
}

private fun RegistrationState.RegistrationError.isFieldError(): Boolean {
    return when (this) {
        RegistrationState.RegistrationError.IDLE,
        RegistrationState.RegistrationError.NETWORK -> false
        else -> true
    }
}
