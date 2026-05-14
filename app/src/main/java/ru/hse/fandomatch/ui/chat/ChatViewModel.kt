package ru.hse.fandomatch.ui.chat

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.logging.Logger
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.usecase.chat.GetChatMessagesPageUseCase
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.UnsubscribeFromChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.media.DownloadMediaToGalleryUseCase
import ru.hse.fandomatch.domain.usecase.media.UploadMediaUseCase
import ru.hse.fandomatch.utils.epochSecondsToDateString

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadChatInfoUseCase: LoadChatInfoUseCase,
    private val subscribeToChatMessagesUseCase: SubscribeToChatMessagesUseCase,
    private val unsubscribeFromChatMessagesUseCase: UnsubscribeFromChatMessagesUseCase,
    private val getChatMessagesPageUseCase: GetChatMessagesPageUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val downloadMediaToGalleryUseCase: DownloadMediaToGalleryUseCase,
    private val logger: Logger,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ChatState> = MutableStateFlow(ChatState.Idle)
    val state: StateFlow<ChatState> get() = _state
    private val _action = MutableStateFlow<ChatAction?>(null)
    val action: StateFlow<ChatAction?> get() = _action

    private var messages: List<Message> = emptyList()
    private var hasMoreOlderMessages: Boolean = true
    private var isLoadingMoreMessages: Boolean = false
    private var activeChat: Chat? = null

    private val stateMutex = Mutex()
    private var subscriptionJob: Job? = null

    private companion object {
        const val MESSAGES_BLOCK_SIZE = 30
    }

    private suspend fun setState(reducer: (ChatState) -> ChatState) {
        withContext(dispatcherMain) {
            stateMutex.withLock {
                _state.value = reducer(_state.value)
            }
        }
    }

    fun obtainEvent(event: ChatEvent) {
        logger.i("ChatViewModel", "Obtained event: $event")
        when (event) {
            ChatEvent.Clear -> clear()
            is ChatEvent.LoadChat -> loadChat(event.profileId)
            is ChatEvent.SendMessage -> sendMessage(event.timestamp)
            is ChatEvent.MessageDraftChanged -> draftChanged(event.draft)
            is ChatEvent.AttachmentsChanged -> attachmentsChanged(event.filesWithTypes)
            is ChatEvent.ProfileClicked -> goToProfile()
            is ChatEvent.DownloadMediaItem -> downloadMediaItem(event.mediaItem)
            ChatEvent.LoadOlderMessages -> loadOlderMessages()
            ChatEvent.ToastShown -> toastShown()
            is ChatEvent.SubscribeToChatUpdates -> subscribe(event.profileId)
            ChatEvent.UnsubscribeFromChatUpdates -> unsubscribe()
        }
    }

    private fun loadChat(profileId: String?) {
        viewModelScope.launch(dispatcherIO) {
            setState { ChatState.Loading }

            if (profileId == null) {
                setState { ChatState.Error }
                return@launch
            }

            messages = emptyList()
            hasMoreOlderMessages = true
            isLoadingMoreMessages = false
            activeChat = null

            loadChatInfoUseCase.execute(userId = profileId)
                .onFailure { exception ->
                    logger.e("ChatViewModel", "Failed to load chat info", exception)
                    setState { ChatState.Error }
                    return@launch
                }
                .onSuccess { chat ->
                    getChatMessagesPageUseCase.execute(
                        chatId = chat.chatId,
                        userId = profileId,
                        beforeTimestamp = null,
                        size = MESSAGES_BLOCK_SIZE,
                    )
                        .onFailure { exception ->
                            logger.e("ChatViewModel", "Failed to load initial messages", exception)
                            setState { ChatState.Error }
                            return@launch
                        }
                        .onSuccess { initialMessages ->
                            messages = initialMessages
                            hasMoreOlderMessages = initialMessages.size >= MESSAGES_BLOCK_SIZE
                            activeChat = chat
                            renderMainState()
                        }
                }
        }
    }

    private fun subscribe(profileId: String?) {
        profileId ?: return
        subscriptionJob?.cancel()
        subscriptionJob = viewModelScope.launch(dispatcherIO) {
            subscribeToChatMessagesUseCase.execute(profileId)
                .onFailure { exception ->
                    logger.e(
                        "ChatViewModel",
                        "Failed to subscribe to chat messages",
                        exception
                    )
                }
                .onSuccess { messagesFlow ->
                    messagesFlow.collect { newMessage ->
                        logger.d("ChatViewModel", "Received new message: $newMessage")
                        messages = listOf(newMessage) + messages
                        renderMainState()
                    }
                }
        }
    }

    private fun unsubscribe() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        unsubscribeFromChatMessagesUseCase.execute()
    }

    private fun loadOlderMessages() {
        val current = _state.value as? ChatState.Main ?: return
        if (isLoadingMoreMessages || !hasMoreOlderMessages) return

        val timestamp = messages.lastOrNull()?.timestamp ?: return
        isLoadingMoreMessages = true
        _state.value = current.copy(isLoadingMore = true)

        viewModelScope.launch(dispatcherIO) {
            getChatMessagesPageUseCase.execute(
                chatId = current.chatId,
                userId = current.participantId,
                beforeTimestamp = timestamp,
                size = MESSAGES_BLOCK_SIZE,
            )
                .onFailure { exception ->
                    logger.e("ChatViewModel", "Failed to load older messages", exception)
                    isLoadingMoreMessages = false
                    withContext(dispatcherMain) {
                        val state = _state.value as? ChatState.Main ?: return@withContext
                        _state.value = state.copy(isLoadingMore = false)
                    }
                }
                .onSuccess { older ->
                    messages = (messages + older)
                        .distinctBy { it.messageId }
                    hasMoreOlderMessages = older.size == MESSAGES_BLOCK_SIZE
                    isLoadingMoreMessages = false
                    renderMainState()
                }
        }
    }

    private fun draftChanged(draft: TextFieldValue) {
        viewModelScope.launch(dispatcherMain) {
            setState { current ->
                (current as? ChatState.Main)?.copy(messageDraft = draft) ?: current
            }
        }
    }

    private fun attachmentsChanged(filesWithTypes: List<Pair<ByteArray, MediaType>>) {
        viewModelScope.launch(dispatcherMain) {
            setState { current ->
                (current as? ChatState.Main)?.copy(attachedFilesWithTypes = filesWithTypes)
                    ?: current
            }
        }
    }

    private fun sendMessage(timestamp: Long) {
        val currentState = _state.value as? ChatState.Main ?: return
        val message = currentState.messageDraft
        val filesWithTypes = currentState.attachedFilesWithTypes
        if (message.text.isBlank() && filesWithTypes.isEmpty()) return
        logger.i("ChatViewModel", "Sending message: $message at $timestamp")
        viewModelScope.launch(dispatcherIO) {
            val mediaIds = filesWithTypes.mapNotNull { (bytes, type) ->
                val uploadInfoResult = uploadMediaUseCase.execute(bytes, type)
                val mediaId = uploadInfoResult.getOrNull()
                mediaId ?: run {
                    logger.e(
                        "ChatViewModel",
                        "Failed to get upload media url",
                        uploadInfoResult.exceptionOrNull()
                    )
                    return@mapNotNull null
                }
                mediaId to type
            }
            if (message.text.isBlank() && mediaIds.isEmpty()) return@launch
            sendMessageUseCase.execute(
                userId = currentState.participantId,
                content = message.text.trim(),
                mediaIdsWithTypes = mediaIds,
                timestamp = timestamp,
            )
                .onFailure { exception ->
                    logger.e("ChatViewModel", "Failed to send message", exception)
                }
                .onSuccess {
                    setState { current ->
                        (current as? ChatState.Main)?.copy(
                            messageDraft = TextFieldValue(""),
                            attachedFilesWithTypes = emptyList()
                        ) ?: current
                    }
                }
        }
    }

    private fun goToProfile() {
        when (val currentState = _state.value) {
            is ChatState.Main -> _action.value = ChatAction.GoToProfile(
                profileId = currentState.participantId
            )

            else -> Unit
        }
    }

    private fun downloadMediaItem(mediaItem: MediaItem) {
        viewModelScope.launch(dispatcherIO) {
            downloadMediaToGalleryUseCase.execute(
                mediaUrl = mediaItem.url,
                mediaType = mediaItem.mediaType
            )
                .onFailure {
                    logger.e("ChatViewModel", "Failed to download media item", it)
                    _action.value = ChatAction.ShowErrorDownloadToast
                }
                .onSuccess {
                    _action.value = ChatAction.ShowSuccessDownloadToast
                }
        }
    }

    private fun toastShown() {
        _action.value = null
    }

    private fun clear() {
        unsubscribe()
        messages = emptyList()
        hasMoreOlderMessages = true
        isLoadingMoreMessages = false
        activeChat = null
        viewModelScope.launch(dispatcherMain) {
            setState { ChatState.Idle }
            _action.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        clear()
    }

    private suspend fun renderMainState() {
        val chat = activeChat ?: return
        setState { current ->
            val currentMain = current as? ChatState.Main
            ChatState.Main(
                chatId = chat.chatId,
                participantId = chat.participantId,
                participantName = chat.participantName,
                participantAvatarUrl = chat.participantAvatarUrl,
                uiElements = messages.mapMessagesToUiElements(),
                hasMoreOlder = hasMoreOlderMessages,
                isLoadingMore = isLoadingMoreMessages,
                attachedFilesWithTypes = currentMain?.attachedFilesWithTypes ?: emptyList(),
                messageDraft = currentMain?.messageDraft ?: TextFieldValue(""),
                error = currentMain?.error ?: ChatState.ChatError.IDLE,
            )
        }
    }

    private fun List<Message>.mapMessagesToUiElements(): List<ChatUiElement> {
        if (isEmpty()) return emptyList()

        val result = mutableListOf<ChatUiElement>()
        var currentDate: String? = firstOrNull()?.timestamp?.epochSecondsToDateString()
        for ((index, message) in withIndex()) {
            val dateString = message.timestamp.epochSecondsToDateString()
            if (dateString != currentDate) {
                result.add(ChatUiElement.DayElement(dateString))
                currentDate = dateString
            }
            val hasTail = index == 0 || this[index - 1].isFromThisUser != message.isFromThisUser
            result.add(
                ChatUiElement.MessageElement(
                    message = message,
                    hasTail = hasTail
                )
            )
        }
        if (result.lastOrNull() !is ChatUiElement.DayElement) {
            val dateString = result.lastOrNull { it is ChatUiElement.MessageElement }
                ?.let { (it as ChatUiElement.MessageElement).message.timestamp.epochSecondsToDateString() }
            dateString?.let {
                result.add(ChatUiElement.DayElement(it))
            }
        }
        return result
    }
}
