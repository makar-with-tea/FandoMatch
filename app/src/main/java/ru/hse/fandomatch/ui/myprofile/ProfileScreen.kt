package ru.hse.fandomatch.ui.myprofile

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.ui.composables.AvatarWithBackground
import ru.hse.fandomatch.ui.composables.EndIcon
import ru.hse.fandomatch.ui.composables.ExpandableText
import ru.hse.fandomatch.ui.composables.FandomCarouselWithDropdown
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.composables.SkeletonView
import ru.hse.fandomatch.ui.navigation.EndIconState
import ru.hse.fandomatch.ui.navigation.TopBarState

@Composable
fun ProfileScreen(
    userId: Long? = null,
    setTopBarState: (TopBarState) -> Unit,
    goToMessages: (Long?) -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()
    Log.i("ProfileScreen", "Rendering MyProfileScreen with state: ${state.value}")

    when (val action = action.value) {
        is ProfileAction.GoToMessages -> {
            goToMessages(action.userId)
            viewModel.obtainEvent(ProfileEvent.Clear)
        }
        null -> {}
    }

    Log.d("ProfileScreen", "State: $state")

    when (val state = state.value) {
        is ProfileState.Main -> MainState(
            state = state,
            setTopBarState = setTopBarState,
            onMessagesClicked = {
                viewModel.obtainEvent(
                    ProfileEvent.MessageButtonClicked(state.id)
                )
            }
        )

        is ProfileState.Loading -> LoadingState()

        ProfileState.Idle -> {
            viewModel.obtainEvent(ProfileEvent.LoadProfile(userId))
            IdleState()
        }

        is ProfileState.Error -> ErrorState(
            error = state.error,
        )
    }
}

@Composable
private fun MainState(
    state: ProfileState.Main,
    setTopBarState: (TopBarState) -> Unit,
    onMessagesClicked: (Long?) -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = @Composable {
                val title = state.login ?: stringResource(R.string.login_hidden)
                MyTitle(title)
            },
            endIcons = when (state.type) {
                ProfileType.OWN -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_edit,
                        onClick = { /* TODO */ },
                        descriptionId = R.string.edit_profile_icon_description
                    ),
                    EndIconState(
                        iconId = R.drawable.ic_settings,
                        onClick = { /* TODO */ },
                        descriptionId = R.string.settings_icon_description
                    )
                )

                ProfileType.FRIEND -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_message,
                        onClick = { onMessagesClicked(state.id) },
                        descriptionId = R.string.go_to_chat_button_description
                    )
                )

                ProfileType.OTHER -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_dislike,
                        onClick = { /* TODO */ },
                        descriptionId = R.string.dislike_profile_description
                    ),
                    EndIconState(
                        iconId = R.drawable.ic_like,
                        onClick = { /* TODO */ },
                        descriptionId = R.string.like_profile_description
                    ),
                )
            }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // header
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(60.dp)
//                .background(MaterialTheme.colorScheme.secondaryContainer)
//                .padding(horizontal = 4.dp),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            IconButton(
//                onClick = onBackClicked,
//            ) {
//                Icon(
//                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
//                    contentDescription = stringResource(R.string.arrow_back_description),
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//
//
//
//
//            Row(
//                modifier = Modifier
//                    .weight(1f),
//                horizontalArrangement = Arrangement.End,
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                val iconsList = when (state.type) {
//                    ProfileType.OWN ->  listOf(
//                        EndIconState(
//                            iconId = R.drawable.ic_edit,
//                            onClick = { /* TODO */ },
//                            description = stringResource(id = R.string.edit_profile_icon_description)
//                        ),
//                        EndIconState(
//                            iconId = R.drawable.ic_settings,
//                            onClick = { /* TODO */ },
//                            description = stringResource(id = R.string.settings_icon_description)
//                        )
//                    )
//
//                    ProfileType.FRIEND -> listOf(
//                        EndIconState(
//                            iconId = R.drawable.ic_message,
//                            onClick = { onMessagesClicked(state.id) },
//                            description = stringResource(id = R.string.go_to_chat_button_description)
//                        )
//                    )
//
//                    ProfileType.OTHER -> listOf(
//                        EndIconState(
//                            iconId = R.drawable.ic_dislike,
//                            onClick = { /* TODO */ },
//                            description = stringResource(id = R.string.dislike_profile_description)
//                        ),
//                        EndIconState(
//                            iconId = R.drawable.ic_like,
//                            onClick = { /* TODO */ },
//                            description = stringResource(id = R.string.like_profile_description)
//                        ),
//                    )
//                }
//
//                iconsList.forEach { state -> EndIcon(state) }
//            }
//        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiaryContainer),
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // header of profile
                AvatarWithBackground(
                    state.backgroundUrl,
                    state.avatarUrl,
                    MaterialTheme.colorScheme.tertiaryContainer
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MyTitle(state.name)

                    FandomCarouselWithDropdown(
                        fandoms = state.fandoms
                    )
                    ExpandableText(
                        text = state.description.orEmpty(),
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                // posts
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    LoadingPosts()
                }
            }
        }
    }
}

@Composable
private fun LoadingPosts() {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, 0, LinearEasing)
            )
            animatable.snapTo(0f)
        }
    }

    val tan = remember { 0.26795f } // tan(15 degrees)
    val screenHeight = with (LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val globalY = screenHeight * animatable.value
    val globalX = tan * globalY

    val endY = screenHeight + globalY
    val endX = tan * endY

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
    ) {
        repeat(5) {
            SkeletonView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp)
                    .clip(shape = RoundedCornerShape(14.dp)),
                globalX = globalX,
                globalY = globalY,
                endX = endX,
                endY = endY,
            )
        }
    }
}

@Composable
private fun LoadingState() = LoadingBlock()

@Composable
private fun IdleState() = LoadingBlock()

@Composable
private fun ErrorState(
    error: ProfileState.ProfileError,
) {
    // todo
    when (error) {
        ProfileState.ProfileError.IDLE -> Text("Idle error")
        ProfileState.ProfileError.NETWORK -> Text("Network error")
        ProfileState.ProfileError.NO_USER -> Text("No such user")
    }
}
@Preview(showBackground = true)
@Composable
fun MainStatePreview() {
    val mockState = ProfileState.Main(
        type = ProfileType.OWN,
        id = mockUser.id,
        login = mockUser.login,
        fandoms = mockUser.fandoms,
        description = mockUser.description,
        name = mockUser.name,
        gender = mockUser.gender,
        age = 25,
        avatarUrl = mockUser.avatarUrl,
        backgroundUrl = mockUser.backgroundUrl,
        city = mockUser.city,
    )

    MainState(
        state = mockState,
        setTopBarState = { },
        onMessagesClicked = { },
    )
}
