package ru.hse.fandomatch.ui.feed

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.FeedPost
import ru.hse.fandomatch.ui.composables.LoadingPosts
import ru.hse.fandomatch.utils.epochMillisToDateString
import ru.hse.fandomatch.ui.composables.BasicErrorState
import ru.hse.fandomatch.ui.composables.MyFloatingButton

@Composable
fun FeedScreen(
    navigateToPost: (String) -> Unit,
    navigateToNewPost: () -> Unit,
    viewModel: FeedViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("FeedScreen", "State: $state")
    when (val action = action.value) {
        is FeedAction.NavigateToPost -> {
            navigateToPost(action.postId)
            viewModel.obtainEvent(FeedEvent.Clear)
        }

        is FeedAction.NavigateToNewPost -> {
            navigateToNewPost()
            viewModel.obtainEvent(FeedEvent.Clear)
        }

        null -> {}
    }

    when (state.value) {
        is FeedState.Main -> {
            MainState(
                state = state.value as FeedState.Main,
                onPostClicked = { id -> viewModel.obtainEvent(FeedEvent.PostClicked(id)) },
                onNewPostClicked = { viewModel.obtainEvent(FeedEvent.NewPostClicked) },
                onPostLiked = { id -> viewModel.obtainEvent(FeedEvent.PostLiked(id)) },
            )
        }
        is FeedState.Idle -> {
            IdleState()
            viewModel.obtainEvent(FeedEvent.LoadPosts)
        }
        is FeedState.Loading -> {
            LoadingState()
        }
        is FeedState.Error -> {
            ErrorState(
                onRetry = { viewModel.obtainEvent(FeedEvent.LoadPosts) },
            )
        }
    }
}

@Composable
private fun MainState(
    state: FeedState.Main,
    onPostClicked: (String) -> Unit,
    onNewPostClicked: () -> Unit,
    onPostLiked: (String) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(state.posts) { post ->
                FeedPost(
                    userName = post.authorName,
                    userLogin = post.authorLogin,
                    userAvatarUrl = post.authorAvatar?.url,
                    postDate = post.timestamp.epochMillisToDateString(),
                    postText = post.content,
                    mediaItems = post.mediaItems,
                    areReactionsAvailable = true,
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    isLiked = post.isLikedByCurrentUser,
                    onLikeClick = { onPostLiked(post.id) },
                    onPostClick = { onPostClicked(post.id) },
                    modifier = Modifier,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    fandoms = post.fandoms,
                )
            }
        }

        MyFloatingButton(
            onClick = { onNewPostClicked() },
            modifier = Modifier.align(Alignment.BottomEnd),
            icon = ImageVector.vectorResource(id = R.drawable.ic_add_post),
            contentDescription = stringResource(R.string.create_post_label),
        )
    }
}

@Composable
private fun LoadingState() = LoadingPosts()

@Composable
private fun IdleState() = LoadingState()

@Composable
private fun ErrorState(
    onRetry: () -> Unit,
) {
    BasicErrorState(onRetry)
}
