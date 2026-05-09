package ru.hse.fandomatch.ui.chatslist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatPreviewsUseCase

class ChatsListViewModel(
    private val subscribeToChatPreviewsUseCase: SubscribeToChatPreviewsUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ChatsListState> =
        MutableStateFlow(ChatsListState.Idle)
    val state: StateFlow<ChatsListState>
        get() = _state
    private val _action = MutableStateFlow<ChatsListAction?>(null)
    val action: StateFlow<ChatsListAction?>
        get() = _action

    private var _allChats: StateFlow<List<ChatPreview>> = MutableStateFlow(emptyList())

    private var currentBatchSize: Int = 0

    private var job: Job? = null

    private companion object {
        const val CHAT_PREVIEWS_PAGE_SIZE = 30
    }

    fun obtainEvent(event: ChatsListEvent) {
        Log.d("ChatsListViewModel", "Obtained event: $event")
        when (event) {
            is ChatsListEvent.ChatClicked -> goToChat(event.chatId)
            is ChatsListEvent.LoadChats -> loadChats()
            is ChatsListEvent.SearchChats -> searchChats(event.query)
            is ChatsListEvent.Clear -> clear()
        }
    }

    private fun goToChat(chatId: String) {
        _action.value = ChatsListAction.NavigateToChat(chatId)
    }

    private fun loadChats() {
        val currentState = _state.value
        if (currentState is ChatsListState.Main) {
            if (currentState.isLoadingMore || !currentState.hasMore) return
            _state.value = currentState.copy(isLoadingMore = true)
        }

        currentBatchSize += CHAT_PREVIEWS_PAGE_SIZE
        job?.cancel()
        job = viewModelScope.launch(dispatcherIO) {
            subscribeToChatPreviewsUseCase.execute(
                size = currentBatchSize,
            )
                .onFailure {
                    Log.e("ChatsListViewModel", "Failed to load chat previews: ${it.message}")
                    withContext(dispatcherMain) {
                        _state.value = ChatsListState.Error
                    }
                }
                .onSuccess { chatPreviewsFlow ->
                    _allChats = chatPreviewsFlow
                    withContext(dispatcherMain) {
                        _state.value = ChatsListState.Main(
                            chats = _allChats.value,
                            filteredByQuery = null,
                            hasMore = _allChats.value.size == currentBatchSize,
                            isLoadingMore = false,
                        )
                    }
                    _allChats.collect {
                        Log.d("ChatsListViewModel", "Loaded chat previews: $it")
                        val query = when (val currentState = _state.value) {
                            is ChatsListState.Main -> currentState.filteredByQuery
                            else -> null
                        }
                        withContext(dispatcherMain) { searchChats(query) }
                    }
                }
        }
    }

    private fun searchChats(query: String?) {
        val currentState = _state.value as? ChatsListState.Main ?: return
        val allChats = _allChats.value
        if (query.isNullOrBlank()) {
            _state.value = currentState.copy(
                filteredByQuery = null,
                chats = allChats,
            )
        } else {
            val filtered = allChats.filter { chatPreview ->
                chatPreview.participantName.contains(query, ignoreCase = true)
            }
            _state.value = currentState.copy(
                filteredByQuery = query,
                chats = filtered,
            )
        }
    }

    private fun clear() {
        _state.value = ChatsListState.Idle
        _action.value = null
        job?.cancel()
    }
}
