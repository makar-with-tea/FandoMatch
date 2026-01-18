package ru.hse.fandomatch.ui.chatslist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.hse.fandomatch.data.mock.mockChatPreviews

class ChatsListViewModel(
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

    fun obtainEvent(event: ChatsListEvent) {
        Log.d("ChatsListViewModel", "Obtained event: $event")
        when (event) {
            is ChatsListEvent.ChatClicked -> goToChat(event.chatId)
            is ChatsListEvent.LoadChats -> loadChats()
            is ChatsListEvent.Clear -> clear()
        }
    }

    private fun goToChat(chatId: Long) {
        _action.value = ChatsListAction.NavigateToChat(chatId)
    }

    private fun loadChats() {
        // todo
        _state.value = ChatsListState.Loading
        viewModelScope.launch(dispatcherIO) {
            delay(1000) // simulate loading

            _state.value = ChatsListState.Main(chats = mockChatPreviews)
        }
    }

    private fun clear() {
        _state.value = ChatsListState.Idle
        _action.value = null
    }
}
