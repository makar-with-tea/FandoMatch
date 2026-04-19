package ru.hse.fandomatch.ui.editprofile

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.ui.composables.AvatarWithBackground
import ru.hse.fandomatch.ui.composables.FandomInput
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyTextField
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.utils.getBytesFromUri
import ru.hse.fandomatch.ui.composables.BasicErrorState
import ru.hse.fandomatch.ui.composables.CityInput
import ru.hse.fandomatch.ui.composables.getName


@Composable
fun EditProfileScreen(
    navigateToAddFandom: () -> Unit,
    navigateToMyProfile: () -> Unit,
    setTopBarState: (TopBarState) -> Unit,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("EditProfileScreen", "State: $state")
    when (action.value) {
        is EditProfileAction.NavigateToAddFandom -> {
            navigateToAddFandom()
            viewModel.obtainEvent(EditProfileEvent.Clear)
        }

        is EditProfileAction.NavigateToMyProfile -> {
            navigateToMyProfile()
            viewModel.obtainEvent(EditProfileEvent.Clear)
        }

        is EditProfileAction.ShowErrorToast -> {
            Toast.makeText(
                LocalContext.current,
                stringResource(R.string.edit_profile_error_toast),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.obtainEvent(EditProfileEvent.ToastShown)
        }

        null -> {}
    }

    when (val currentState = state.value) {
        is EditProfileState.Main -> {
            MainState(
                state = currentState,
                setTopBarState = setTopBarState,
                onAvatarChanged = { avatar ->
                    viewModel.obtainEvent(EditProfileEvent.AvatarChanged(avatar))
                },
                onBackgroundChanged = { background ->
                    viewModel.obtainEvent(EditProfileEvent.BackgroundChanged(background))
                },
                onNameChanged = { name ->
                    viewModel.obtainEvent(EditProfileEvent.NameChanged(name))
                },
                onDescriptionChanged = { description ->
                    viewModel.obtainEvent(EditProfileEvent.DescriptionChanged(description))
                },
                onFandomAdded = { fandom ->
                    viewModel.obtainEvent(EditProfileEvent.FandomAdded(fandom))
                },
                onFandomRemoved = { fandom ->
                    viewModel.obtainEvent(EditProfileEvent.FandomRemoved(fandom))
                },
                onFandomSearch = { query ->
                    viewModel.obtainEvent(EditProfileEvent.FandomSearched(query))
                },
                onCitySelected = { city ->
                    viewModel.obtainEvent(EditProfileEvent.CitySelected(city))
                },
                onCitySearch = { query ->
                    viewModel.obtainEvent(EditProfileEvent.CitySearched(query))
                },
                onSave = {
                    viewModel.obtainEvent(EditProfileEvent.SaveButtonClicked)
                },
                onSuggestFandomButtonClicked = {
                    viewModel.obtainEvent(EditProfileEvent.AddFandomButtonClicked)
                }
            )
        }
        is EditProfileState.Loading -> {
            LoadingState()
        }
        is EditProfileState.Idle -> {
            IdleState()
            viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        }

        is EditProfileState.Error -> {
            ErrorState(
                onRetry = {
                    viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
                }
            )
        }
    }
}

@Composable
private fun MainState(
    state: EditProfileState.Main,
    setTopBarState: (TopBarState) -> Unit,
    onAvatarChanged: (ByteArray?) -> Unit,
    onBackgroundChanged: (ByteArray?) -> Unit,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onFandomAdded: (Fandom) -> Unit,
    onFandomRemoved: (Fandom) -> Unit,
    onFandomSearch: (String?) -> Unit,
    onCitySelected: (City) -> Unit,
    onCitySearch: (String?) -> Unit,
    onSuggestFandomButtonClicked: () -> Unit,
    onSave: () -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = @Composable {
                Row {
                    MyTitle(state.login)
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.save_profile_button),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.onSecondaryContainer)
                            .clickable { onSave() }
                            .padding(8.dp)
                    )
                }
            },
            endIcons = listOf()
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        item {
            val context = LocalContext.current
            var isPickingAvatar by remember { mutableStateOf(false) }
            val pickMedia =
                rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: $uri")
                        val avatarByteArray = getBytesFromUri(context, uri)
                        if (avatarByteArray == null) {
                            Log.d("PhotoPicker", "Failed to read image bytes")
                            Toast.makeText(context, R.string.avatar_error, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            if (isPickingAvatar) onAvatarChanged(avatarByteArray)
                            else onBackgroundChanged(avatarByteArray)
                        }
                    } else {
                        Log.d("PhotoPicker", "No media selected")
                        if (isPickingAvatar) onAvatarChanged(null)
                        else onBackgroundChanged(null)
                    }
                }

            AvatarWithBackground(
                state.background?.url,
                state.avatar?.url,
                state.backgroundBytes,
                state.avatarBytes,
                onEditAvatar = {
                    isPickingAvatar = true
                    pickMedia.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onEditBackground = {
                    isPickingAvatar = false
                    pickMedia.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        }

        item {
            var name by remember { mutableStateOf(state.name) }
            MyTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                value = name,
                label = stringResource(R.string.name_label),
                isError = state.nameError != EditProfileState.EditProfileError.IDLE,
                enabled = true,
                errorText = when (state.nameError) {
                    EditProfileState.EditProfileError.NAME_LENGTH -> stringResource(R.string.name_length_error)
                    EditProfileState.EditProfileError.NAME_CONTENT -> stringResource(R.string.name_content_error)
                    else -> null
                },
                onValueChange = {
                    name = it
                    onNameChanged(it)
                }
            )
        }

        item {
            var description by remember { mutableStateOf(state.description.orEmpty()) }
            MyTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                value = description,
                label = stringResource(R.string.description_label),
                isError = state.descriptionError != EditProfileState.EditProfileError.IDLE,
                enabled = true,
                errorText = when (state.descriptionError) {
                    EditProfileState.EditProfileError.DESCRIPTION_LENGTH -> stringResource(R.string.description_length_error)
                    else -> null
                },
                onValueChange = {
                    description = it
                    onDescriptionChanged(it)
                },
                hideOnDone = false,
            )
        }

        item {
            Column {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    fontSize = 18.sp,
                    text = stringResource(R.string.city_filter_label, state.city?.getName() ?: stringResource(R.string.city_filter_no_city)),
                )
                CityInput(
                    foundCities = state.foundCities,
                    onCitySelected = onCitySelected,
                    onSearch = onCitySearch,
                    areCitiesLoading = state.areCitiesLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Column {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    fontSize = 18.sp,
                    text = stringResource(R.string.your_fandoms_label),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.did_not_found_fandom_label),
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = {
                            onSuggestFandomButtonClicked()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.here_you_go_button),
                            fontSize = 14.sp
                        )
                    }
                }
                FandomInput(
                    foundFandoms = state.foundFandoms,
                    selectedFandoms = state.fandoms,
                    onFandomAdded = onFandomAdded,
                    onFandomRemoved = onFandomRemoved,
                    onSearch = onFandomSearch,
                    areFandomsLoading = state.areFandomsLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LoadingState() {
    LoadingBlock()
}

@Composable
private fun IdleState() {
    LoadingBlock()
}

@Composable
private fun ErrorState(
    onRetry: () -> Unit,
) {
    BasicErrorState(onRetry)
}