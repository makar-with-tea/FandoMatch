package ru.hse.fandomatch.ui.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun obtainEvent(event: FeedEvent) {
        Log.d("FeedViewModel", "Obtained event: $event")
        when (event) {
            is FeedEvent.PostClicked -> goToPost(event.postId)
            is FeedEvent.LoadPosts -> loadPosts()
            is FeedEvent.PostLiked -> likePost(event.postId)
            is FeedEvent.Clear -> clear()
        }
    }

    private fun goToPost(chatId: String) {
        _action.value = FeedAction.NavigateToPost(chatId)
    }

    private fun loadPosts() {
        viewModelScope.launch(dispatcherIO) {
            val result = getFeedUseCase.execute()
            val posts = result.getOrNull() ?: run {
                Log.e("FeedViewModel", "Posts are null")
                withContext(dispatcherMain) {
                    _state.value = FeedState.Error
                }
                return@launch
            }
            Log.d("FeedViewModel", "Loaded posts: $posts")
            _state.value = FeedState.Main(posts = posts)
        }
    }

    private fun likePost(postId: String) {
        viewModelScope.launch(dispatcherIO) {
            val result = likePostUseCase.execute(postId)
            if (result.isFailure) {
                Log.e("FeedViewModel", "Failed to like post $postId", result.exceptionOrNull())
                return@launch
            }
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

    private fun clear() {
        _state.value = FeedState.Idle
        _action.value = null
    }
}
