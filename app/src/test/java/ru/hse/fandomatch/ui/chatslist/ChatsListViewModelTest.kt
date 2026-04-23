package ru.hse.fandomatch.ui.chatslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
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
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatPreviewsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ChatsListViewModel
    private lateinit var subscribeToChatPreviewsUseCase: SubscribeToChatPreviewsUseCase
    private lateinit var chatsFlow: MutableStateFlow<List<ChatPreview>>
    private val testDispatcher = StandardTestDispatcher()

    private val chat1 = ChatPreview(
        chatId = "1",
        participantName = "Luffy",
        participantAvatarUrl = "url1",
        lastMessage = "Yo",
        isLastMessageFromThisUser = false,
        lastMessageTimestamp = 1000L,
        newMessagesCount = 2,
    )
    private val chat2 = ChatPreview(
        chatId = "2",
        participantName = "Nami",
        participantAvatarUrl = "url2",
        lastMessage = "Hi",
        isLastMessageFromThisUser = true,
        lastMessageTimestamp = 2000L,
        newMessagesCount = 0,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        subscribeToChatPreviewsUseCase = mock(SubscribeToChatPreviewsUseCase::class.java)
        chatsFlow = MutableStateFlow(emptyList())
        runBlocking {
            `when`(subscribeToChatPreviewsUseCase.execute()).thenReturn(Result.success(chatsFlow))
        }
        viewModel = ChatsListViewModel(
            subscribeToChatPreviewsUseCase = subscribeToChatPreviewsUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load chats success updates state`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = listOf(chat1, chat2)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ChatsListState.Main)
        state as ChatsListState.Main
        assertEquals(listOf(chat1, chat2), state.chats)
        assertEquals(null, state.filteredByQuery)
    }

    @Test
    fun `load chats failure updates error state`() = runTest {
        `when`(subscribeToChatPreviewsUseCase.execute()).thenReturn(Result.failure(RuntimeException()))
        viewModel = ChatsListViewModel(
            subscribeToChatPreviewsUseCase = subscribeToChatPreviewsUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )

        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        advanceUntilIdle()

        assertEquals(ChatsListState.Error, viewModel.state.value)
    }

    @Test
    fun `search chats filters by query`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = listOf(chat1, chat2)
        advanceUntilIdle()

        viewModel.obtainEvent(ChatsListEvent.SearchChats("nam"))

        val state = viewModel.state.value as ChatsListState.Main
        assertEquals(listOf(chat2), state.chats)
        assertEquals("nam", state.filteredByQuery)
    }

    @Test
    fun `search chats with blank query returns all chats`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = listOf(chat1, chat2)
        advanceUntilIdle()

        viewModel.obtainEvent(ChatsListEvent.SearchChats(""))

        val state = viewModel.state.value as ChatsListState.Main
        assertEquals(listOf(chat1, chat2), state.chats)
        assertEquals(null, state.filteredByQuery)
    }

    @Test
    fun `chat clicked emits navigate action`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.ChatClicked("2"))

        assertEquals(ChatsListAction.NavigateToChat("2"), viewModel.action.value)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = listOf(chat1)
        advanceUntilIdle()
        viewModel.obtainEvent(ChatsListEvent.ChatClicked("1"))

        viewModel.obtainEvent(ChatsListEvent.Clear)

        assertEquals(ChatsListState.Idle, viewModel.state.value)
        assertEquals(null, viewModel.action.value)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}