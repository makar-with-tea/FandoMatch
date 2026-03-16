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
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import ru.hse.fandomatch.domain.model.ChatPreview
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatPreviewsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ChatsListViewModel
    private lateinit var subscribeToChatPreviewsUseCase: SubscribeToChatPreviewsUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val chat1 = ChatPreview(
        chatId = 1L,
        participantName = "Luffy",
        participantAvatarUrl = "url1",
        lastMessage = "Yo",
        isLastMessageFromThisUser = false,
        lastMessageTimestamp = 1000L,
        newMessagesCount = 2
    )
    private val chat2 = ChatPreview(
        chatId = 2L,
        participantName = "Nami",
        participantAvatarUrl = "url2",
        lastMessage = "Hi",
        isLastMessageFromThisUser = true,
        lastMessageTimestamp = 2000L,
        newMessagesCount = 0
    )
    private val allChats = listOf(chat1, chat2)
    private lateinit var chatsFlow: MutableStateFlow<List<ChatPreview>>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        subscribeToChatPreviewsUseCase = Mockito.mock(SubscribeToChatPreviewsUseCase::class.java)
        chatsFlow = MutableStateFlow(emptyList())
        runBlocking {
            Mockito.`when`(subscribeToChatPreviewsUseCase.execute()).thenReturn(chatsFlow)
        }
        viewModel = ChatsListViewModel(
            subscribeToChatPreviewsUseCase = subscribeToChatPreviewsUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher
        )
    }


    @Test
    fun `initial state is Idle`() = runTest {
        Assert.assertTrue(viewModel.state.value is ChatsListState.Idle)
    }

    @Test
    fun `LoadChats event sets Loading then Main state with chats`() = runTest {
        // Act
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = allChats
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        Assert.assertTrue(state is ChatsListState.Main)
        state as ChatsListState.Main
        Assert.assertEquals(allChats, state.chats)
        Assert.assertEquals(null, state.filteredByQuery)
    }

    @Test
    fun `SearchChats event filters chats by query`() = runTest {
        // Arrange
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = allChats
        advanceUntilIdle()

        // Act
        viewModel.obtainEvent(ChatsListEvent.SearchChats("nam"))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        Assert.assertTrue(state is ChatsListState.Main)
        state as ChatsListState.Main
        Assert.assertEquals(listOf(chat2), state.chats)
        Assert.assertEquals("nam", state.filteredByQuery)
    }

    @Test
    fun `SearchChats with blank query returns all chats`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = allChats
        advanceUntilIdle()

        viewModel.obtainEvent(ChatsListEvent.SearchChats(""))
        advanceUntilIdle()

        val state = viewModel.state.value
        Assert.assertTrue(state is ChatsListState.Main)
        state as ChatsListState.Main
        Assert.assertEquals(allChats, state.chats)
        Assert.assertEquals(null, state.filteredByQuery)
    }

    @Test
    fun `ChatClicked event sets NavigateToChat action`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.ChatClicked(2L))
        val action = viewModel.action.value
        Assert.assertEquals(ChatsListAction.NavigateToChat(2L), action)
    }

    @Test
    fun `Clear event resets state and action`() = runTest {
        viewModel.obtainEvent(ChatsListEvent.LoadChats)
        chatsFlow.value = allChats
        advanceUntilIdle()
        viewModel.obtainEvent(ChatsListEvent.ChatClicked(1L))

        viewModel.obtainEvent(ChatsListEvent.Clear)
        advanceUntilIdle()

        Assert.assertTrue(viewModel.state.value is ChatsListState.Idle)
        Assert.assertEquals(null, viewModel.action.value)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}