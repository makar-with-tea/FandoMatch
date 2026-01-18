package ru.hse.fandomatch.ui.chatslist

import ru.hse.fandomatch.domain.model.ChatPreview

sealed class ChatsListState {
    enum class ChatsListError {
        NETWORK,
        IDLE
    }
    data class Main(
        val chats: List<ChatPreview>,
    ) : ChatsListState()

    data object Loading : ChatsListState()
    data object Idle : ChatsListState()
}

sealed class ChatsListEvent {
    data class ChatClicked(
        val chatId: Long,
    ): ChatsListEvent()
    // todo other events: delete, mute, etc.
    data object LoadChats: ChatsListEvent()
    data object Clear: ChatsListEvent()
}

sealed class ChatsListAction {
    data class NavigateToChat(val chatId: Long) : ChatsListAction()
}
