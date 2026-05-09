package ru.hse.fandomatch.ui.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.usecase.posts.GetFeedUseCase
import ru.hse.fandomatch.domain.usecase.posts.LikePostUseCase

class FeedViewModel(
    private val getFeedUseCase: GetFeedUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<FeedState> =
        MutableStateFlow(FeedState.Idle)
    val state: StateFlow<FeedState>
        get() = _state
    private val _action = MutableStateFlow<FeedAction?>(null)
    val action: StateFlow<FeedAction?>
        get() = _action

    private var currentPosts: List<Post> = emptyList()
    private var hasMore: Boolean = true
    private var isLoadingMore: Boolean = false

    private companion object {
        const val PAGE_SIZE = 30
    }

    fun obtainEvent(event: FeedEvent) {
        Log.d("FeedViewModel", "Obtained event: $event")
        when (event) {
            is FeedEvent.PostClicked -> goToPost(event.postId)
            is FeedEvent.LoadPosts -> loadPosts()
            is FeedEvent.LoadMorePosts -> loadMorePosts()
            is FeedEvent.PostLiked -> likePost(event.postId)
            FeedEvent.NewPostClicked -> _action.value = FeedAction.NavigateToNewPost
            is FeedEvent.Clear -> clear()
        }
    }

    private fun goToPost(chatId: String) {
        _action.value = FeedAction.NavigateToPost(chatId)
    }

    private fun loadPosts() {
        viewModelScope.launch(dispatcherIO) {
            _state.value = FeedState.Loading
            getFeedUseCase.execute(
                beforeTimestamp = null,
                size = PAGE_SIZE,
            )
                .onFailure {
                    Log.e("FeedViewModel", "Failed to load feed posts: ${it.message}")
                    withContext(dispatcherMain) {
                        _state.value = FeedState.Error
                    }
                    return@launch
                }
                .onSuccess { posts ->
                    Log.d("FeedViewModel", "Successfully loaded feed posts: $posts")
                    currentPosts = posts
                    hasMore = posts.size >= PAGE_SIZE
                    withContext(dispatcherMain) {
                        _state.value = FeedState.Main(
                            posts = currentPosts,
                            hasMore = hasMore,
                            isLoadingMore = false
                        )
                    }
                }
        }
    }

    private fun loadMorePosts() {
        if (isLoadingMore || !hasMore) return
        isLoadingMore = true
        val oldestTimestamp =
            currentPosts.minOfOrNull { it.timestamp } ?: System.currentTimeMillis()
        viewModelScope.launch(dispatcherIO) {
            withContext(dispatcherMain) {
                val current = _state.value as? FeedState.Main
                if (current != null) _state.value = current.copy(isLoadingMore = true)
            }

            val result = getFeedUseCase.execute(
                beforeTimestamp = oldestTimestamp,
                size = PAGE_SIZE,
            )
            result.onFailure { exception ->
                Log.e("FeedViewModel", "Failed to load more posts: ${exception.message}")
                withContext(dispatcherMain) {
                    val current = _state.value as? FeedState.Main
                    if (current != null) _state.value = current.copy(isLoadingMore = false)
                }
                isLoadingMore = false
                return@launch
            }

            result.onSuccess { older ->
                currentPosts =
                    (currentPosts + older).distinctBy { it.id }.sortedByDescending { it.timestamp }
                hasMore = older.size >= PAGE_SIZE
                withContext(dispatcherMain) {
                    _state.value = FeedState.Main(
                        posts = currentPosts,
                        hasMore = hasMore,
                        isLoadingMore = false
                    )
                }
                isLoadingMore = false
            }
        }
    }

    private fun likePost(postId: String) {
        viewModelScope.launch(dispatcherIO) {
            likePostUseCase.execute(postId)
                .onFailure { exception ->
                    Log.e("FeedViewModel", "Failed to like post $postId", exception)
                }
                .onSuccess {
                    Log.d("FeedViewModel", "Liked post $postId successfully")
                    withContext(dispatcherMain) {
                        val currentState = _state.value as? FeedState.Main ?: return@withContext
                        val updatedPosts = currentState.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(
                                    likeCount = if (post.isLikedByCurrentUser) post.likeCount - 1 else post.likeCount + 1,
                                    isLikedByCurrentUser = !post.isLikedByCurrentUser,
                                )
                            } else {
                                post
                            }
                        }
                        _state.value = currentState.copy(posts = updatedPosts)
                    }
                }
        }
    }

    private fun clear() {
        _state.value = FeedState.Idle
        _action.value = null
    }
}

private const val PAGE_SIZE = 2 // todo 50