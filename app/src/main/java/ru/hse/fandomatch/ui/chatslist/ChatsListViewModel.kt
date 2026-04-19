package ru.hse.fandomatch.ui.chatslist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
        viewModelScope.launch(dispatcherIO) {
            val result = subscribeToChatPreviewsUseCase.execute()
            val chatPreviewsFlow = result.getOrNull() ?: run {
                Log.e("ChatsListViewModel", "Failed to subscribe to chat previews: ${result.exceptionOrNull()}")
                withContext(dispatcherMain) {
                    _state.value = ChatsListState.Error
                }
                return@launch
            }
            _allChats = chatPreviewsFlow
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

    private fun searchChats(query: String?) {
        val allChats = _allChats.value
        if (query.isNullOrBlank()) {
            _state.value = ChatsListState.Main(chats = allChats)
        } else {
            val filtered = allChats.filter { chatPreview ->
                chatPreview.participantName.contains(query, ignoreCase = true)
            }
            _state.value = ChatsListState.Main(
                filteredByQuery = query,
                chats = filtered,
            )
        }
    }

    private fun clear() {
        _state.value = ChatsListState.Idle
        _action.value = null
    }
}
