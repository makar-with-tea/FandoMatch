package ru.hse.fandomatch.ui.feed

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.ui.composables.FeedPost
import ru.hse.fandomatch.ui.composables.LoadingPosts
import ru.hse.fandomatch.timestampToDateString

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
                modifier = Modifier.clickable { onPostClicked(post.id) },
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
}

@Composable
private fun LoadingState() = LoadingPosts()

@Composable
private fun IdleState() = LoadingState()

@Preview(showBackground = true)
@Composable
private fun FeedScreenPreview() {
    MainState(
        FeedState.Main(
            posts = listOf(
                Post(
                    id = "1",
                    authorId = "1",
                    authorName = "John Doe",
                    authorLogin = "johndoe",
                    authorAvatarUrl = "dzimbei",
                    timestamp = System.currentTimeMillis() - 3600_000, // 1 hour ago
                    content = "Hello, this is my first post! I'm so excited to be here and share my thoughts with you all. This app looks amazing, and I can't wait to connect with fellow fans!",
                    imageUrls = listOf("dzimbei"),
                    likeCount = 5,
                    commentCount = 2,
                    isLikedByCurrentUser = false,
                ),
                Post(
                    id = "2",
                    authorId = "2",
                    authorName = "Jane Smith",
                    authorLogin = "janesmith",
                    authorAvatarUrl = "what_is_written_here",
                    timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
                    content = "Just watched the latest episode of JJK, and it was incredible!!",
                    imageUrls = listOf("what_is_written_here"),
                    likeCount = 10,
                    commentCount = 4,
                    isLikedByCurrentUser = true,
                )
            ),
        ),
        onPostClicked = {}
    )
}
