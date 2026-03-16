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
import ru.hse.fandomatch.domain.usecase.feed.GetFeedUseCase

class FeedViewModel(
    private val getFeedUseCase: GetFeedUseCase,
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
            is FeedEvent.PostClicked -> goToPost(event.chatId)
            is FeedEvent.LoadPosts -> loadPosts()
            is FeedEvent.Clear -> clear()
        }
    }

    private fun goToPost(chatId: Long) {
        _action.value = FeedAction.NavigateToPost(chatId)
    }

    private fun loadPosts() {
        // todo
        viewModelScope.launch(dispatcherIO) {
            delay(1000) // simulate loading

            // todo error handling
            val posts = getFeedUseCase.execute()
            Log.d("FeedViewModel", "Loaded posts: $posts")
            _state.value = FeedState.Main(posts = posts)
        }
    }

    private fun clear() {
        _state.value = FeedState.Idle
        _action.value = null
    }
}
