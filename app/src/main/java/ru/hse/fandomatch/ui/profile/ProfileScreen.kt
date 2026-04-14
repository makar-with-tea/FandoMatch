package ru.hse.fandomatch.ui.profile

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.navigation.EndIconState
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.timestampToDateString
import ru.hse.fandomatch.ui.composables.AvatarAndNameBlock
import ru.hse.fandomatch.ui.composables.AvatarWithBackground
import ru.hse.fandomatch.ui.composables.CityAndGenderText
import ru.hse.fandomatch.ui.composables.ExpandableText
import ru.hse.fandomatch.ui.composables.FandomCarouselWithDropdown
import ru.hse.fandomatch.ui.composables.FeedPost
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyFloatingButton
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.theme.FandoMatchTheme

@Composable
fun ProfileScreen(
    userId: String? = null,
    isCurrentUser: Boolean = false,
    setTopBarState: (TopBarState) -> Unit,
    goToMessages: (String?) -> Unit,
    goToEditProfile: () -> Unit,
    goToSettings: () -> Unit,
    goToAddPost: () -> Unit,
    goToMatches: () -> Unit,
    goToProfile: (String) -> Unit,
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

        is ProfileAction.GoToAddPost -> {
            goToAddPost()
            viewModel.obtainEvent(ProfileEvent.Clear)
        }

        is ProfileAction.GoToMatches -> {
            goToMatches()
            viewModel.obtainEvent(ProfileEvent.Clear)
        }

        is ProfileAction.GoToProfile -> {
            goToProfile(action.profileId)
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
                viewModel.obtainEvent(ProfileEvent.MessageButtonClicked(state.id))
            },
            onEditProfileClicked = {
                viewModel.obtainEvent(ProfileEvent.EditProfileButtonClicked)
            },
            onSettingsClicked = {
                viewModel.obtainEvent(ProfileEvent.SettingsButtonClicked)
            },
            onAddPostClicked = {
                viewModel.obtainEvent(ProfileEvent.AddPostButtonClicked)
            },
            onLikeClicked = {
                viewModel.obtainEvent(ProfileEvent.LikeButtonClicked(state.id))
            },
            onDislikeClicked = {
                viewModel.obtainEvent(ProfileEvent.DislikeButtonClicked(state.id))
            },
            onPostsClicked = {
                viewModel.obtainEvent(ProfileEvent.PostsButtonClicked)
            },
            onFriendsClicked = {
                viewModel.obtainEvent(ProfileEvent.FriendsButtonClicked)
            },
            onRequestsClicked = {
                viewModel.obtainEvent(ProfileEvent.RequestsButtonClicked)
            },
            onProfileClicked = { profileId ->
                viewModel.obtainEvent(ProfileEvent.ProfileClicked(profileId))
            }
        )

        is ProfileState.Loading -> LoadingState()

        ProfileState.Idle -> {
            viewModel.obtainEvent(ProfileEvent.LoadProfile(userId, isCurrentUser))
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
    onMessagesClicked: (String?) -> Unit,
    onEditProfileClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onAddPostClicked: () -> Unit,
    onLikeClicked: () -> Unit,
    onDislikeClicked: () -> Unit,
    onPostsClicked: () -> Unit,
    onFriendsClicked: () -> Unit,
    onRequestsClicked: () -> Unit,
    onProfileClicked: (String) -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = @Composable {
                val title = state.login ?: stringResource(R.string.login_hidden)
                MyTitle(title)
            },
            endIcons = when (state.type) {
                is ProfileType.Own -> listOf(
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

                is ProfileType.Friend -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_message,
                        onClick = { onMessagesClicked(state.id) },
                        descriptionId = R.string.go_to_chat_button_description
                    )
                )

                ProfileType.Stranger -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_dislike,
                        onClick = { onDislikeClicked() },
                        descriptionId = R.string.dislike_profile_description
                    ),
                    EndIconState(
                        iconId = R.drawable.ic_like,
                        onClick = { onLikeClicked() },
                        descriptionId = R.string.like_profile_description
                    ),
                )
            }
        )
    )

    Box {
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
                    ) {
                        MyTitle(
                            text = "${state.name}, ${state.age}",
                            padding = 0.dp
                        )
                        CityAndGenderText(
                            city = state.city,
                            gender = state.gender,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        FandomCarouselWithDropdown(
                            fandoms = state.fandoms
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExpandableText(
                            text = state.description.orEmpty(),
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (state.type is ProfileType.Own) {
                        CompositionLocalProvider(
                            LocalMinimumInteractiveComponentSize provides 0.dp
                        ) {
                            MultiChoiceSegmentedButtonRow(
                                space = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                val shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    bottomStart = 12.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 12.dp
                                )
                                val borderStroke = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                )
                                val colors = SegmentedButtonColors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    activeBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                    inactiveContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    inactiveContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    inactiveBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                    disabledActiveContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    disabledActiveContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                    disabledActiveBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                    disabledInactiveContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    disabledInactiveContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                    disabledInactiveBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                )
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = 0,
                                        count = 3,
                                        baseShape = shape,
                                    ),
                                    border = borderStroke,
                                    colors = colors,
                                    onCheckedChange = { onPostsClicked() },
                                    checked = state.bottomSheetState is ProfileState.BottomSheetState.Posts,
                                    icon = {},
                                    label = { Text(stringResource(R.string.posts_label)) }
                                )
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = 1,
                                        count = 3,
                                        baseShape = shape,
                                    ),
                                    border = borderStroke,
                                    colors = colors,
                                    onCheckedChange = { onFriendsClicked() },
                                    checked = state.bottomSheetState is ProfileState.BottomSheetState.Friends,
                                    icon = {},
                                    label = { Text(stringResource(R.string.friends_label)) }
                                )
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = 2,
                                        count = 3,
                                        baseShape = shape,
                                    ),
                                    border = borderStroke,
                                    colors = colors,
                                    onCheckedChange = { onRequestsClicked() },
                                    checked = state.bottomSheetState is ProfileState.BottomSheetState.Requests,
                                    icon = {},
                                    label = { Text(stringResource(R.string.your_requests_label)) }
                                )
                            }
                        }
                    }
                }
            }

            // posts
            when (val bottomSheetState = state.bottomSheetState) {
                is ProfileState.BottomSheetState.Posts -> {
                    items(bottomSheetState.posts) { post ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                        ) {
                            FeedPost(
                                userName = post.authorName,
                                userLogin = post.authorLogin,
                                userAvatarUrl = post.authorAvatarUrl,
                                postDate = post.timestamp.timestampToDateString(),
                                postText = post.content,
                                imageUrls = post.imageUrls,
                                areReactionsAvailable = state.type is ProfileType.Own
                                        || state.type is ProfileType.Friend,
                                likeCount = post.likeCount,
                                commentCount = post.commentCount,
                                isLiked = post.isLikedByCurrentUser,
                                onLikeClick = {}, // todo
                                onCommentClick = {}, // todo
                                onImageClicked = { _, _ -> }, // todo
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

                is ProfileState.BottomSheetState.Friends -> {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(bottomSheetState.friends) { friend ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                        ) {
                            AvatarAndNameBlock(
                                name = friend.name,
                                avatarUrl = friend.avatarUrl,
                                login = friend.login,
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                avatarSize = 36.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onProfileClicked(friend.id) }
                            )

                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
                is ProfileState.BottomSheetState.Requests -> {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(bottomSheetState.requests) { possibleFriend ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                        ) {
                            AvatarAndNameBlock(
                                name = possibleFriend.name,
                                avatarUrl = possibleFriend.avatarUrl,
                                login = possibleFriend.login,
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                avatarSize = 36.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onProfileClicked(possibleFriend.id) }
                            )

                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        if (state.type is ProfileType.Own && state.bottomSheetState is ProfileState.BottomSheetState.Posts) {
            MyFloatingButton(
                onClick = { onAddPostClicked() },
                modifier = Modifier.align(Alignment.BottomEnd),
                icon = ImageVector.vectorResource(id = R.drawable.ic_add_post),
                contentDescription = stringResource(R.string.create_post_label),
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
        type = ProfileType.Own(login = "mocklogin", email = "mockemail"),
        id = mockUser.id,
        login = (mockUser.profileType as? ProfileType.Own)?.login,
        fandoms = mockUser.fandoms,
        description = mockUser.description,
        name = mockUser.name,
        gender = mockUser.gender,
        age = 25,
        avatarUrl = mockUser.avatarUrl,
        backgroundUrl = mockUser.backgroundUrl,
        city = mockUser.city,
        bottomSheetState = ProfileState.BottomSheetState.Posts(
            posts = listOf(),
            isError = false
        )
    )

    FandoMatchTheme {
        MainState(
            state = mockState,
            setTopBarState = { },
            onMessagesClicked = { },
            onEditProfileClicked = { },
            onSettingsClicked = { },
            onAddPostClicked = { },
            onLikeClicked = { },
            onDislikeClicked = { },
            onPostsClicked = { },
            onFriendsClicked = { },
            onRequestsClicked = { },
            onProfileClicked = { },
        )
    }
}
