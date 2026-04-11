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
    data object Idle : FeedState()
}

sealed class FeedEvent {
    data class PostClicked(
        val chatId: String,
    ): FeedEvent()
    data object LoadPosts: FeedEvent()
//    data class SearchPosts(val query: String?): FeedEvent() todo мы умеем искать?
    data object Clear: FeedEvent()
}

sealed class FeedAction {
    data class NavigateToPost(val postId: String) : FeedAction()
}
