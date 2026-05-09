package ru.hse.fandomatch.ui.feed

import ru.hse.fandomatch.domain.model.Post

sealed class FeedState {
    data class Main(
        val posts: List<Post>,
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false,
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
    data object LoadMorePosts: FeedEvent()
    data object NewPostClicked: FeedEvent()
    data object Clear: FeedEvent()
}

sealed class FeedAction {
    data class NavigateToPost(val postId: String) : FeedAction()
    data object NavigateToNewPost : FeedAction()
}
