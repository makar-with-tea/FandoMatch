package ru.hse.fandomatch.ui.feed

import ru.hse.fandomatch.domain.model.Post

sealed class FeedState {
    enum class FeedError {
        NETWORK,
        IDLE
    }
    data class Main(
        val posts: List<Post>,
    ) : FeedState()

    data object Loading : FeedState()
    data object Error : FeedState()
    data object Idle : FeedState()
}

sealed class FeedEvent {
    data class PostClicked(
        val postId: String,
    ): FeedEvent()
    data class PostLiked(
        val postId: String,
    ): FeedEvent()
    data object LoadPosts: FeedEvent()
    data object Clear: FeedEvent()
}

sealed class FeedAction {
    data class NavigateToPost(val postId: String) : FeedAction()
}
