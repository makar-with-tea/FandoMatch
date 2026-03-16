package ru.hse.fandomatch.ui.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.ui.composables.AvatarWithBackground
import ru.hse.fandomatch.ui.composables.ExpandableText
import ru.hse.fandomatch.ui.composables.FandomCarouselWithDropdown
import ru.hse.fandomatch.ui.composables.FeedPost
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.navigation.EndIconState
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.timestampToDateString

@Composable
fun ProfileScreen(
    userId: Long? = null,
    setTopBarState: (TopBarState) -> Unit,
    goToMessages: (Long?) -> Unit,
    goToEditProfile: () -> Unit,
    goToSettings: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()
    Log.i("ProfileScreen", "Rendering ProfileScreen with state: ${state.value}")

    when (val action = action.value) {
        is ProfileAction.GoToMessages -> {
            goToMessages(action.userId)
            viewModel.obtainEvent(ProfileEvent.Clear)
        }

        ProfileAction.GoToEditProfile -> {
            goToEditProfile()
            viewModel.obtainEvent(ProfileEvent.Clear)
        }

        ProfileAction.GoToSettings -> {
            goToSettings()
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
            },
            onEditProfileClicked = {
                viewModel.obtainEvent(ProfileEvent.EditProfileButtonClicked)
            },
            onSettingsClicked = {
                viewModel.obtainEvent(ProfileEvent.SettingsButtonClicked)
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
    onEditProfileClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
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
                        onClick = { onEditProfileClicked() },
                        descriptionId = R.string.edit_profile_icon_description
                    ),
                    EndIconState(
                        iconId = R.drawable.ic_settings,
                        onClick = { onSettingsClicked() },
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // header of profile
                AvatarWithBackground(
                    state.backgroundUrl,
                    state.avatarUrl,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
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
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // posts
        items(state.posts) { post ->
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                FeedPost(
                    userName = post.authorName,
                    userLogin = post.authorLogin,
                    userAvatarUrl = post.authorAvatarUrl,
                    postDate = post.timestamp.timestampToDateString(),
                    postText = post.content,
                    imageUrls = post.imageUrls,
                    areReactionsAvailable = true, // todo
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    isLiked = post.isLikedByCurrentUser,
                    onLikeClick = {},
                    onCommentClick = {},
                    onImageClicked = { _, _ -> },
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                )
                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth()
                )
            }
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
        onEditProfileClicked = { },
        onSettingsClicked = { },
    )
}
