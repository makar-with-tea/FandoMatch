package ru.hse.fandomatch.ui.chat

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ChatViewModel
    private lateinit var globalRepository: GlobalRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        globalRepository = mock(GlobalRepository::class.java)

        viewModel = ChatViewModel(
            sendMessageUseCase = SendMessageUseCase(globalRepository),
            loadChatInfoUseCase = LoadChatInfoUseCase(globalRepository),
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher
        )
    }

    @Test
    fun `loadChat with null userId sets error state`() = runTest {
        // Act
        viewModel.obtainEvent(ChatEvent.LoadChat(null))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        assertEquals(ChatState.Error, state)
        verify(globalRepository, never()).loadChatInfo(org.mockito.Mockito.anyLong())
    }

    @Test
    fun `loadChat with valid userId sets loading state immediately`() = runTest {
        // Arrange
        val userId = 10L
        val chat = createChat(
            messages = listOf(
                Message(
                    messageId = 1L,
                    isFromThisUser = false,
                    content = "hello",
                    timestamp = 1000L
                )
            )
        )
        `when`(globalRepository.loadChatInfo(userId)).thenReturn(chat)

        // Act
        viewModel.obtainEvent(ChatEvent.LoadChat(userId))

        // Assert
        val state = viewModel.state.first()
        assertEquals(ChatState.Loading, state)
    }

    @Test
    fun `loadChat with valid userId maps chat info to main state`() = runTest {
        // Arrange
        val userId = 10L
        val chat = createChat(
            chatId = 5L,
            participantId = 99L,
            participantName = "Nami",
            participantAvatarUrl = "avatar_url",
            messages = listOf(
                Message(
                    messageId = 1L,
                    isFromThisUser = false,
                    content = "hello",
                    timestamp = 1000L
                )
            )
        )
        `when`(globalRepository.loadChatInfo(userId)).thenReturn(chat)

        // Act
        viewModel.obtainEvent(ChatEvent.LoadChat(userId))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        assertTrue(state is ChatState.Main)

        state as ChatState.Main
        assertEquals(5L, state.chatId)
        assertEquals(99L, state.participantId)
        assertEquals("Nami", state.participantName)
        assertEquals("avatar_url", state.participantAvatarUrl)
        assertEquals(ChatState.ChatError.IDLE, state.error)

        verify(globalRepository).loadChatInfo(userId)
    }

    @Test
    fun `loadChat reverses messages and calculates tails correctly`() = runTest {
        // Arrange
        val userId = 10L
        val message1 = Message(
            messageId = 1L,
            isFromThisUser = true,
            content = "first",
            timestamp = 1000L
        )
        val message2 = Message(
            messageId = 2L,
            isFromThisUser = true,
            content = "second",
            timestamp = 2000L
        )
        val message3 = Message(
            messageId = 3L,
            isFromThisUser = false,
            content = "third",
            timestamp = 3000L
        )

        val chat = createChat(messages = listOf(message1, message2, message3))
        `when`(globalRepository.loadChatInfo(userId)).thenReturn(chat)

        // Act
        viewModel.obtainEvent(ChatEvent.LoadChat(userId))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as ChatState.Main

        assertEquals(3, state.uiElements.size)

        assertEquals(message3, state.uiElements[0].first)
        assertEquals(true, state.uiElements[0].second)

        assertEquals(message2, state.uiElements[1].first)
        assertEquals(true, state.uiElements[1].second)

        assertEquals(message1, state.uiElements[2].first)
        assertEquals(false, state.uiElements[2].second)
    }

    @Test
    fun `sendMessage from main state adds new message to top`() = runTest {
        // Arrange
        val userId = 10L
        val existingMessage = Message(
            messageId = 1L,
            isFromThisUser = false,
            content = "old message",
            timestamp = 1000L
        )
        val chat = createChat(
            participantId = 77L,
            messages = listOf(existingMessage)
        )
        `when`(globalRepository.loadChatInfo(userId)).thenReturn(chat)

        viewModel.obtainEvent(ChatEvent.LoadChat(userId))
        advanceUntilIdle()

        val images = listOf(byteArrayOf(1, 2, 3))

        // Act
        viewModel.obtainEvent(
            ChatEvent.SendMessage(
                message = "new message",
                images = images,
                timestamp = 123L
            )
        )
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as ChatState.Main

        assertEquals(2, state.uiElements.size)

        val newMessage = state.uiElements[0].first
        val newMessageNeedsTail = state.uiElements[0].second

        assertEquals(2L, newMessage.messageId)
        assertEquals(true, newMessage.isFromThisUser)
        assertEquals("new message", newMessage.content)
        assertEquals(123000L, newMessage.timestamp)
        assertEquals(listOf("luffy"), newMessage.imageUrls)
        assertEquals(true, newMessageNeedsTail)
    }

    @Test
    fun `sendMessage from main state removes tail from previous newest message`() = runTest {
        // Arrange
        val userId = 10L
        val existingMessage = Message(
            messageId = 1L,
            isFromThisUser = false,
            content = "old message",
            timestamp = 1000L
        )
        val chat = createChat(
            participantId = 77L,
            messages = listOf(existingMessage)
        )
        `when`(globalRepository.loadChatInfo(userId)).thenReturn(chat)

        viewModel.obtainEvent(ChatEvent.LoadChat(userId))
        advanceUntilIdle()

        // Act
        viewModel.obtainEvent(
            ChatEvent.SendMessage(
                message = "new message",
                images = emptyList(),
                timestamp = 123L
            )
        )
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as ChatState.Main
        val previousMessageNeedsTail = state.uiElements[1].second

        assertEquals(false, previousMessageNeedsTail)
    }

    @Test
    fun `sendMessage calls repository with participantId and timestamp in milliseconds`() = runTest {
        // Arrange
        val userId = 10L
        val chat = createChat(
            participantId = 77L,
            messages = emptyList()
        )
        `when`(globalRepository.loadChatInfo(userId)).thenReturn(chat)

        viewModel.obtainEvent(ChatEvent.LoadChat(userId))
        advanceUntilIdle()

        val images = listOf(byteArrayOf(5, 6, 7))

        // Act
        viewModel.obtainEvent(
            ChatEvent.SendMessage(
                message = "hello",
                images = images,
                timestamp = 321L
            )
        )
        advanceUntilIdle()

        // Assert
        verify(globalRepository).sendMessage(
            77L,
            "hello",
            images,
            321000L
        )
    }

    @Test
    fun `sendMessage when state is not main does not change state`() = runTest {
        // Act
        viewModel.obtainEvent(
            ChatEvent.SendMessage(
                message = "hello",
                images = emptyList(),
                timestamp = 111L
            )
        )
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        assertEquals(ChatState.Idle, state)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        // Arrange
        val userId = 10L
        val chat = createChat(messages = emptyList())
        `when`(globalRepository.loadChatInfo(userId)).thenReturn(chat)

        viewModel.obtainEvent(ChatEvent.LoadChat(userId))
        advanceUntilIdle()

        // Act
        viewModel.obtainEvent(ChatEvent.Clear)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        val action = viewModel.action.first()

        assertEquals(ChatState.Idle, state)
        assertEquals(null, action)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createChat(
        chatId: Long = 1L,
        participantId: Long = 2L,
        participantName: String = "Luffy",
        participantAvatarUrl: String? = "avatar",
        messages: List<Message>
    ): Chat {
        return Chat(
            chatId = chatId,
            participantId = participantId,
            participantName = participantName,
            participantAvatarUrl = participantAvatarUrl,
            messages = messages
        )
    }
}
