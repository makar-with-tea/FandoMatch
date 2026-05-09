package ru.hse.fandomatch.ui.chatslist

import ru.hse.fandomatch.domain.model.ChatPreview

sealed class ChatsListState {
    data class Main(
        val chats: List<ChatPreview>,
        val filteredByQuery: String? = null,
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false,
    ) : ChatsListState()

    data object Loading : ChatsListState()
    data object Error : ChatsListState()
    data object Idle : ChatsListState()
}

sealed class ChatsListEvent {
    data class ChatClicked(
        val chatId: String,
    ): ChatsListEvent()
    data object LoadChats: ChatsListEvent()
    data class SearchChats(val query: String?): ChatsListEvent()
    data object Clear: ChatsListEvent()
}

sealed class ChatsListAction {
    data class NavigateToChat(val chatId: String) : ChatsListAction()
}
