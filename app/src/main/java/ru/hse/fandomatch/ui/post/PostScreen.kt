package ru.hse.fandomatch.ui.post

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.epochMillisToDateString
import ru.hse.fandomatch.ui.composables.AvatarAndNameBlock
import ru.hse.fandomatch.ui.composables.BasicErrorState
import ru.hse.fandomatch.ui.composables.FullPost
import ru.hse.fandomatch.ui.composables.ImagesScreen
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.PostComment

@Composable
fun PostScreen(
    postId: String?,
    setTopBarState: (TopBarState?) -> Unit,
    goToProfile: (String) -> Unit,
    viewModel: PostViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    when (val action = action.value) {
        is PostAction.GoToProfile -> {
            goToProfile(action.profileId)
            viewModel.obtainEvent(PostEvent.Clear)
        }

        null -> Unit
    }

    Log.d("PostScreen", "State: $state")

    when (state.value) {
        is PostState.Main -> MainState(
            state = state.value as PostState.Main,
            setTopBarState = setTopBarState,
            onSendComment = { viewModel.obtainEvent(PostEvent.SendComment) },
            onClickProfile = { viewModel.obtainEvent(PostEvent.ProfileClicked) },
            onClickLike = { viewModel.obtainEvent(PostEvent.LikeClicked) },
            onClickImages = { viewModel.obtainEvent(PostEvent.ImagesClicked) },
            onCloseImages = { viewModel.obtainEvent(PostEvent.ImagesClosed) },
            onUpdateCommentDraft = { commentDraft -> viewModel.obtainEvent(PostEvent.UpdateCommentDraft(commentDraft)) },
        )

        is PostState.Idle -> {
            IdleState()
            viewModel.obtainEvent(PostEvent.LoadPost(postId))
        }

        is PostState.Loading -> LoadingState()

        is PostState.Error -> ErrorState(
            onRetry = { viewModel.obtainEvent(PostEvent.LoadPost(postId)) }
        )
    }
}

@Composable
private fun MainState(
    state: PostState.Main,
    setTopBarState: (TopBarState?) -> Unit,
    onSendComment: () -> Unit,
    onClickProfile: () -> Unit,
    onClickLike: () -> Unit,
    onClickImages: () -> Unit,
    onCloseImages: () -> Unit,
    onUpdateCommentDraft: (String) -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = {
                AvatarAndNameBlock(
                    name = state.fullPost.post.authorName,
                    avatarUrl = state.fullPost.post.authorAvatar?.url,
                    login = state.fullPost.post.authorLogin,
                    onClick = { onClickProfile() },
                )
            },
        )
    )

    var itemsForScreen by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var currentItemIndex by remember { mutableStateOf(0) }
    BackHandler(enabled = itemsForScreen.isNotEmpty()) {
        itemsForScreen = emptyList()
        currentItemIndex = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
        ) {
            item {
                FullPost(
                    postDate = state.fullPost.post.timestamp.epochMillisToDateString(),
                    postText = state.fullPost.post.content,
                    mediaItems = state.fullPost.post.mediaItems,
                    areReactionsAvailable = true,
                    likeCount = state.fullPost.post.likeCount,
                    commentCount = state.fullPost.post.commentCount,
                    fandoms = state.fullPost.post.fandoms,
                    isLiked = state.fullPost.post.isLikedByCurrentUser,
                    onLikeClick = { onClickLike() },
                    onItemClick = { urls, index ->
                        itemsForScreen = urls // todo move to view model
                        currentItemIndex = index
                    },
                    modifier = Modifier,
                    backgroundColor = MaterialTheme.colorScheme.background,
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(state.fullPost.comments.reversed()) {
                PostComment(
                    comment = it,
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                )
            }
        }

        var commentDraft by remember { mutableStateOf(state.commentDraft) }
        OutlinedTextField(
            value = commentDraft,
            onValueChange = {
                onUpdateCommentDraft(it)
                commentDraft = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            placeholder = {
                Text(text = stringResource(R.string.type_a_comment))
            },
            maxLines = 4,
            suffix = {
                IconButton(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (commentDraft.isBlank())
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    enabled = commentDraft.isNotBlank(),
                    onClick = {
                        onSendComment()
                        commentDraft = ""
                        onUpdateCommentDraft("")
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_send),
                        contentDescription = stringResource(R.string.send_comment_button_description),
                    )
                }
            }
        )
    }

    if (itemsForScreen.isNotEmpty()) {
        ImagesScreen(
            items = itemsForScreen,
            initialPage = currentItemIndex,
            titleContent = {
                // todo: from <user>, <time>
            },
            setTopBarState = setTopBarState,
        )
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
