package ru.hse.fandomatch.ui.chat

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import ru.hse.fandomatch.domain.model.Chat
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.media.DownloadMediaToGalleryUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ChatViewModel
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var loadChatInfoUseCase: LoadChatInfoUseCase
    private lateinit var subscribeToChatMessagesUseCase: SubscribeToChatMessagesUseCase
    private lateinit var uploadMediaUseCase: UploadMediaUseCase
    private lateinit var downloadMediaToGalleryUseCase: DownloadMediaToGalleryUseCase
    private lateinit var messagesFlow: MutableStateFlow<List<Message>>
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sendMessageUseCase = mock(SendMessageUseCase::class.java)
        loadChatInfoUseCase = mock(LoadChatInfoUseCase::class.java)
        subscribeToChatMessagesUseCase = mock(SubscribeToChatMessagesUseCase::class.java)
        uploadMediaUseCase = mock(UploadMediaUseCase::class.java)
        downloadMediaToGalleryUseCase = mock(DownloadMediaToGalleryUseCase::class.java)
        messagesFlow = MutableStateFlow(emptyList())
        viewModel = ChatViewModel(
            sendMessageUseCase = sendMessageUseCase,
            loadChatInfoUseCase = loadChatInfoUseCase,
            subscribeToChatMessagesUseCase = subscribeToChatMessagesUseCase,
            uploadMediaUseCase = uploadMediaUseCase,
            downloadMediaToGalleryUseCase = downloadMediaToGalleryUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load chat with null profile id sets error state`() = runTest {
        viewModel.obtainEvent(ChatEvent.LoadChat(null))
        advanceUntilIdle()

        assertEquals(ChatState.Error, viewModel.state.first())
        verify(loadChatInfoUseCase, never()).execute(org.mockito.Mockito.anyString())
    }

    @Test
    fun `load chat success sets main state`() = runTest {
        `when`(loadChatInfoUseCase.execute("10")).thenReturn(Result.success(chat()))
        `when`(subscribeToChatMessagesUseCase.execute("10", "chat-1")).thenReturn(Result.success(messagesFlow))
        messagesFlow.value = listOf(message("1", false, "hello", 1000L))

        viewModel.obtainEvent(ChatEvent.LoadChat("10"))
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is ChatState.Main)
        state as ChatState.Main
        assertEquals("chat-1", state.chatId)
        assertEquals("10", state.participantId)
        assertEquals("Nami", state.participantName)
    }

    @Test
    fun `load chat failure sets error state`() = runTest {
        `when`(loadChatInfoUseCase.execute("10")).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(ChatEvent.LoadChat("10"))
        advanceUntilIdle()

        assertEquals(ChatState.Error, viewModel.state.first())
    }

    @Test
    fun `message draft and attachments events update state`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(ChatEvent.MessageDraftChanged("draft"))
        viewModel.obtainEvent(ChatEvent.AttachmentsChanged(listOf(byteArrayOf(1, 2, 3) to MediaType.IMAGE)))

        val state = viewModel.state.first() as ChatState.Main
        assertEquals("draft", state.messageDraft)
        assertEquals(1, state.attachedFilesWithTypes.size)
    }

    @Test
    fun `send message success clears draft and attachments`() = runTest {
        prepareMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(ChatEvent.MessageDraftChanged("hello"))
        viewModel.obtainEvent(ChatEvent.AttachmentsChanged(listOf(byteArrayOf(1, 2, 3) to MediaType.IMAGE)))
        `when`(uploadMediaUseCase.execute(byteArrayOf(1, 2, 3), MediaType.IMAGE)).thenReturn(Result.success("media-id"))
        `when`(
            sendMessageUseCase.execute(
                userId = "participant-1",
                content = "hello",
                mediaIdsWithTypes = listOf("media-id" to MediaType.IMAGE),
                timestamp = 123000L,
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(ChatEvent.SendMessage(timestamp = 123L))
        advanceUntilIdle()

        val state = viewModel.state.first() as ChatState.Main
        assertEquals("", state.messageDraft)
        assertTrue(state.attachedFilesWithTypes.isEmpty())
    }

    @Test
    fun `send message blank with no attachments does nothing`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(ChatEvent.SendMessage(timestamp = 123L))
        advanceUntilIdle()

        val state = viewModel.state.first() as ChatState.Main
        assertEquals("", state.messageDraft)
        assertTrue(state.attachedFilesWithTypes.isEmpty())
    }

    @Test
    fun `profile clicked emits navigation action`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(ChatEvent.ProfileClicked)

        assertEquals(ChatAction.GoToProfile("participant-1"), viewModel.action.first())
    }

    @Test
    fun `download media success and failure emit actions`() = runTest {
        prepareMainState()
        advanceUntilIdle()
        `when`(downloadMediaToGalleryUseCase.execute("url", MediaType.IMAGE)).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(ChatEvent.DownloadMediaItem(MediaItem("1", MediaType.IMAGE, "url")))
        advanceUntilIdle()
        assertEquals(ChatAction.ShowSuccessDownloadToast, viewModel.action.first())

        `when`(downloadMediaToGalleryUseCase.execute("url2", MediaType.IMAGE)).thenReturn(Result.failure(RuntimeException()))
        viewModel.obtainEvent(ChatEvent.DownloadMediaItem(MediaItem("2", MediaType.IMAGE, "url2")))
        advanceUntilIdle()
        assertEquals(ChatAction.ShowErrorDownloadToast, viewModel.action.first())

        viewModel.obtainEvent(ChatEvent.ToastShown)
        assertEquals(null, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        prepareMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(ChatEvent.Clear)
        advanceUntilIdle()

        assertEquals(ChatState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun prepareMainState() {
        `when`(loadChatInfoUseCase.execute("10")).thenReturn(Result.success(chat()))
        `when`(subscribeToChatMessagesUseCase.execute("10", "chat-1")).thenReturn(Result.success(messagesFlow))
        messagesFlow.value = listOf(message("1", false, "hello", 1000L))
        viewModel.obtainEvent(ChatEvent.LoadChat("10"))
    }

    private fun chat() = Chat(
        chatId = "chat-1",
        participantId = "participant-1",
        participantName = "Nami",
        participantAvatarUrl = "avatar-url",
    )

    private fun message(id: String, fromThisUser: Boolean, content: String, timestamp: Long) = Message(
        messageId = id,
        isFromThisUser = fromThisUser,
        content = content,
        timestamp = timestamp,
    )
}
