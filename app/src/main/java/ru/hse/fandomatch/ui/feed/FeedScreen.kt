package ru.hse.fandomatch.ui.feed

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.ui.composables.FeedPost
import ru.hse.fandomatch.ui.composables.LoadingPosts
import ru.hse.fandomatch.epochMillisToDateString

@Composable
fun FeedScreen(
    navigateToPost: (String) -> Unit,
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

        null -> {}
    }

    when (state.value) {
        is FeedState.Main -> {
            MainState(
                state = state.value as FeedState.Main,
                onPostClicked = { id -> viewModel.obtainEvent(FeedEvent.PostClicked(id)) },
            )
        }
        is FeedState.Idle -> {
            IdleState()
            viewModel.obtainEvent(FeedEvent.LoadPosts)
        }
        is FeedState.Loading -> {
            LoadingState()
        }
    }
}

@Composable
private fun MainState(
    state: FeedState.Main,
    onPostClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(state.posts) { post ->
            FeedPost(
                userName = post.authorName,
                userLogin = post.authorLogin,
                userAvatarUrl = post.authorAvatarUrl,
                postDate = post.timestamp.epochMillisToDateString(),
                postText = post.content,
                imageUrls = post.mediaItems,
                areReactionsAvailable = true,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                isLiked = post.isLikedByCurrentUser,
                onLikeClick = {}, // TODO
                onPostClick = { onPostClicked(post.id) },
                modifier = Modifier,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                fandoms = post.fandoms,
            )
        }
    }
}

@Composable
private fun LoadingState() = LoadingPosts()

@Composable
private fun IdleState() = LoadingState()
