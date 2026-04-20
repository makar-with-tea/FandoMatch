package ru.hse.fandomatch.ui.settings

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.AccountButton
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyAlertDialog
import ru.hse.fandomatch.ui.composables.MyPasswordField
import ru.hse.fandomatch.ui.composables.MySwitch
import ru.hse.fandomatch.ui.composables.MyTextField
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.ui.registration.RegistrationState
import ru.hse.fandomatch.ui.registration.getText
import ru.hse.fandomatch.ui.theme.FandoMatchTheme

@Composable
fun SettingsScreen(
    setTopBarState: (TopBarState) -> Unit,
    navigateToIntro: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val isScreenActive = remember { mutableStateOf(true) }

    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("SettingsScreen", "State: $state")

    when (action.value) {
        is SettingsAction.NavigateToIntro -> {
            isScreenActive.value = false
            navigateToIntro()
            viewModel.obtainEvent(SettingsEvent.Clear)
            return
        }

        null -> {}
    }

    when (state.value) {
        is SettingsState.Main -> {
            MainState(
                state.value as SettingsState.Main,
                setTopBarState = setTopBarState,
                onChangePassword = {
                    viewModel.obtainEvent(SettingsEvent.EditPasswordButtonClicked)
                },
                onChangeEmail = {
                    viewModel.obtainEvent(SettingsEvent.EditEmailButtonClicked)
                },
                onDeleteAccount = {
                    viewModel.obtainEvent(
                        SettingsEvent.DeleteAccountButtonClicked
                    )
                },
                onLogout = { viewModel.obtainEvent(SettingsEvent.LogoutButtonClicked) },
                onMatchNotificationsToggled = {
                    viewModel.obtainEvent(SettingsEvent.MatchNotificationsToggled)
                },
                onMessageNotificationsToggled = {
                    viewModel.obtainEvent(SettingsEvent.MessageNotificationsToggled)
                },
                onShowPostsToNonMatchesToggled = {
                    viewModel.obtainEvent(SettingsEvent.HideMyPostsFromNonMatchesToggled)
                }
            )
        }

        is SettingsState.Loading -> {
            LoadingState()
        }

        is SettingsState.ChangePassword -> {
            ChangePasswordState(
                state.value as SettingsState.ChangePassword,
                setTopBarState = setTopBarState,
                onSavePassword = { newPassword, oldPassword, newPasswordRepeat ->
                    viewModel.obtainEvent(
                        SettingsEvent.SavePasswordButtonClicked(
                            newPassword,
                            oldPassword,
                            newPasswordRepeat
                        )
                    )
                },
                onShowOldPassword = {
                    viewModel.obtainEvent(SettingsEvent.ShowOldPasswordButtonClicked)
                },
                onShowNewPassword = {
                    viewModel.obtainEvent(SettingsEvent.ShowNewPasswordButtonClicked)
                },
                onShowNewPasswordRepeat = {
                    viewModel.obtainEvent(SettingsEvent.ShowNewPasswordRepeatButtonClicked)
                },
                onBackPressed = {
                    viewModel.obtainEvent(SettingsEvent.Back)
                }
            )
        }

        is SettingsState.ChangeEmail -> {
            ChangeEmailState(
                state.value as SettingsState.ChangeEmail,
                setTopBarState = setTopBarState,
                onSubmitEmail = {
                    viewModel.obtainEvent(SettingsEvent.SaveEmailButtonClicked)
                },
                onEmailChanged = { email ->
                    viewModel.obtainEvent(SettingsEvent.EmailChanged(email))
                },
                onSubmitCode = { code ->
                    viewModel.obtainEvent(SettingsEvent.CodeSubmitted(code))
                },
                onBackPressed = {
                    viewModel.obtainEvent(SettingsEvent.Back)
                }
            )
        }

        is SettingsState.Idle -> {
            IdleState()
            if (!isScreenActive.value) {
                return
            }
            viewModel.obtainEvent(SettingsEvent.LoadProfileData)
        }

        is SettingsState.Error -> {
            ErrorState(
                errorText = (state.value as SettingsState.Error).error.toText(),
                onDismiss = {
                    viewModel.obtainEvent(SettingsEvent.LogoutButtonClicked)
                },
                onConfirm = {
                    viewModel.obtainEvent(SettingsEvent.LoadProfileData)
                },
                setTopBarState = setTopBarState
            )
        }

        is SettingsState.DeletionError -> {
            ErrorState(
                errorText = stringResource(R.string.account_delete_error),
                onDismiss = {
                    viewModel.obtainEvent(SettingsEvent.LoadProfileData)
                },
                onConfirm = {
                    viewModel.obtainEvent(
                        SettingsEvent.DeleteAccountButtonClicked
                    )
                },
                setTopBarState = setTopBarState
            )
        }
    }
}

@Composable
fun IdleState() {
    LoadingBlock()
}

@Composable
fun LoadingState() {
    LoadingBlock()
}

@Composable
fun MainState(
    state: SettingsState.Main,
    setTopBarState: (TopBarState) -> Unit,
    onChangePassword: () -> Unit,
    onChangeEmail: () -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
    onMatchNotificationsToggled: () -> Unit,
    onMessageNotificationsToggled: () -> Unit,
    onShowPostsToNonMatchesToggled: () -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = {
                MyTitle(stringResource(id = R.string.settings_title))
            },
            endIcons = emptyList()
        )
    )

    val showDeleteDialog = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.notification_settings_title),
                fontSize = 18.sp,
            )
            MySwitch(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start),
                label = stringResource(id = R.string.match_notifications_label),
                isChecked = state.matchNotificationsEnabled,
                onCheckedChange = { onMatchNotificationsToggled() }
            )
            MySwitch(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start),
                label = stringResource(id = R.string.message_notifications_label),
                isChecked = state.messageNotificationsEnabled,
                onCheckedChange = { onMessageNotificationsToggled() }
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.privacy_settings_title),
                fontSize = 18.sp,
            )
            MySwitch(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start),
                label = stringResource(id = R.string.hide_my_posts_from_non_matches),
                isChecked = state.hideMyPostsFromNonMatches,
                onCheckedChange = { onShowPostsToNonMatchesToggled() }
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )

        AccountButton(
            textId = R.string.change_password_button,
            iconId = R.drawable.ic_lock,
            onClick = onChangePassword
        )


        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )

        AccountButton(
            textId = R.string.change_email_button,
            iconId = R.drawable.ic_mail,
            onClick = { onChangeEmail() }
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )

        AccountButton(
            textId = R.string.logout_button,
            iconId = R.drawable.ic_logout,
            onClick = onLogout
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )

        AccountButton(
            textId = R.string.delete_account_button,
            iconId = R.drawable.ic_delete,
            onClick = { showDeleteDialog.value = true },
            isDangerous = true
        )
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(stringResource(id = R.string.delete_account_title)) },
            text = { Text(stringResource(id = R.string.delete_account_confirmation)) },
            confirmButton = {
                Button(onClick = { onDeleteAccount() }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog.value = false }) {
                    Text(stringResource(id = R.string.no))
                }
            }
        )
    }
}

@Composable
fun ChangePasswordState(
    state: SettingsState.ChangePassword,
    setTopBarState: (TopBarState) -> Unit,
    onSavePassword: (String, String, String) -> Unit,
    onShowOldPassword: () -> Unit,
    onShowNewPassword: () -> Unit,
    onShowNewPasswordRepeat: () -> Unit,
    onBackPressed: () -> Unit
) {
    setTopBarState(
        TopBarState(
            titleContent = {
                MyTitle(stringResource(id = R.string.change_password_title))
            },
            endIcons = emptyList()
        )
    )

    val oldPassword: MutableState<String> = remember { mutableStateOf(state.oldPassword) }
    val newPassword: MutableState<String> = remember { mutableStateOf(state.newPassword) }
    val newPasswordRepeat: MutableState<String> = remember { mutableStateOf(state.newPassword) }

    BackHandler {
        onBackPressed()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
    ) {
        MyPasswordField(
            value = oldPassword.value,
            label = stringResource(id = R.string.old_password_label),
            isError = state.oldPasswordError != SettingsState.SettingsError.IDLE,
            errorText = if (state.oldPasswordError != SettingsState.SettingsError.NETWORK)
                state.oldPasswordError.toText() else null,
            onValueChange = { oldPassword.value = it },
            onIconClick = { onShowOldPassword() },
            passwordVisibility = state.oldPasswordVisibility
        )
        MyPasswordField(
            value = newPassword.value,
            label = stringResource(id = R.string.new_password_label),
            isError = state.newPasswordError != SettingsState.SettingsError.IDLE,
            errorText = if (state.newPasswordError != SettingsState.SettingsError.NETWORK)
                state.newPasswordError.toText() else null,
            onValueChange = { newPassword.value = it },
            onIconClick = { onShowNewPassword() },
            passwordVisibility = state.newPasswordVisibility
        )
        MyPasswordField(
            value = newPasswordRepeat.value,
            label = stringResource(id = R.string.repeat_new_password_label),
            isError = state.newPasswordRepeatError != SettingsState.SettingsError.IDLE,
            errorText = state.newPasswordRepeatError.toText(),
            onValueChange = { newPasswordRepeat.value = it },
            onIconClick = { onShowNewPasswordRepeat() },
            passwordVisibility = state.newPasswordRepeatVisibility
        )
        Button(onClick = {
            onSavePassword(
                newPassword.value,
                oldPassword.value,
                newPasswordRepeat.value
            )
        }) {
            Text(stringResource(id = R.string.save_button))
        }
    }
    if (state.isLoading) {
        LoadingBlock()
    }
}

@Composable
fun ChangeEmailState(
    state: SettingsState.ChangeEmail,
    setTopBarState: (TopBarState) -> Unit,
    onSubmitEmail: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onSubmitCode: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    setTopBarState(
        TopBarState(
            titleContent = {
                MyTitle(stringResource(id = R.string.change_email_title))
            },
            endIcons = emptyList()
        )
    )
    BackHandler {
        onBackPressed()
    }

    if (state.isCode) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            var code by remember { mutableStateOf("") }

            Text(
                text = stringResource(id = R.string.code_verification_description),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MyTextField(
                value = code,
                label = stringResource(id = R.string.verification_code_label),
                isError = state.codeError != SettingsState.SettingsError.IDLE,
                errorText = state.codeError.toText(),
                onValueChange = { code = it.filter { ch -> ch.isDigit() } },
                keyboardType = KeyboardType.NumberPassword,
            )

            Button(
                enabled = !state.isLoading,
                onClick = { onSubmitCode(code) }
            ) { Text(stringResource(R.string.change_email_button)) }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
        ) {
            MyTextField(
                value = state.email,
                onValueChange = {
                    onEmailChanged(it)
                },
                label = stringResource(id = R.string.email_label),
                isError = state.emailError != SettingsState.SettingsError.IDLE,
                errorText = state.emailError.toText(),
                keyboardType = KeyboardType.Email,
            )
            Button(onClick = { onSubmitEmail() }) {
                Text(stringResource(id = R.string.next_step))
            }
        }
    }
}

@Composable
fun ErrorState(
    errorText: String?,
    setTopBarState: (TopBarState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    setTopBarState(
        TopBarState(
            titleContent = {
                MyTitle(stringResource(id = R.string.settings_title))
            },
            endIcons = emptyList()
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        MyAlertDialog(
            title = stringResource(id = R.string.loading_error),
            text = errorText ?: stringResource(id = R.string.unknown_error),
            onConfirm = onConfirm,
            onDismissRequest = onDismiss,
            confirmButtonText = stringResource(id = R.string.retry_button_text)
        )
    }
}

@Composable
fun SettingsState.SettingsError.toText() = when (this) {
    SettingsState.SettingsError.IDLE -> null
    SettingsState.SettingsError.PASSWORD_LENGTH -> stringResource(id = R.string.password_length_error)
    SettingsState.SettingsError.PASSWORD_CONTENT ->
        stringResource(id = R.string.password_content_error)

    SettingsState.SettingsError.PASSWORD_MISMATCH ->
        stringResource(id = R.string.password_mismatch_error)

    SettingsState.SettingsError.PASSWORD_INCORRECT ->
        stringResource(id = R.string.password_incorrect_error)

    SettingsState.SettingsError.NETWORK ->
        stringResource(id = R.string.network_error)

    SettingsState.SettingsError.NETWORK_FATAL ->
        stringResource(id = R.string.network_error_long)

    SettingsState.SettingsError.ACCOUNT_NOT_FOUND ->
        stringResource(id = R.string.account_not_found_error)

    SettingsState.SettingsError.EMAIL_CONTENT ->
        stringResource(id = R.string.email_content_error)

    SettingsState.SettingsError.INVALID_CODE ->
        stringResource(id = R.string.invalid_code_error)
}

@Preview(showBackground = true)
@Composable
fun MainStatePreview() {
    FandoMatchTheme {
        MainState(
            state = SettingsState.Main(
                email = "johndoe@example.com",
            ),
            onChangePassword = {},
            onDeleteAccount = {},
            onLogout = {},
            onChangeEmail = {},
            setTopBarState = {},
            onMatchNotificationsToggled = {},
            onMessageNotificationsToggled = {},
            onShowPostsToNonMatchesToggled = {}
        )
    }
}