package ru.hse.fandomatch.ui.feed

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
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.usecase.posts.GetFeedUseCase
import ru.hse.fandomatch.domain.usecase.posts.LikePostUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FeedViewModel
    private lateinit var getFeedUseCase: GetFeedUseCase
    private lateinit var likePostUseCase: LikePostUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getFeedUseCase = mock(GetFeedUseCase::class.java)
        likePostUseCase = mock(LikePostUseCase::class.java)
        viewModel = FeedViewModel(
            getFeedUseCase = getFeedUseCase,
            likePostUseCase = likePostUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load posts success updates state with posts`() = runTest {
        val post = createPost(id = "1", isLiked = false, likeCount = 3)
        `when`(getFeedUseCase.execute()).thenReturn(Result.success(listOf(post)))

        viewModel.obtainEvent(FeedEvent.LoadPosts)
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is FeedState.Main)
        assertEquals(listOf(post), (state as FeedState.Main).posts)
    }

    @Test
    fun `load posts failure updates error state`() = runTest {
        `when`(getFeedUseCase.execute()).thenReturn(Result.failure(RuntimeException("network")))

        viewModel.obtainEvent(FeedEvent.LoadPosts)
        advanceUntilIdle()

        assertEquals(FeedState.Error, viewModel.state.first())
    }

    @Test
    fun `post clicked emits navigate to post action`() = runTest {
        viewModel.obtainEvent(FeedEvent.PostClicked("post-42"))

        assertEquals(FeedAction.NavigateToPost("post-42"), viewModel.action.first())
    }

    @Test
    fun `new post clicked emits navigate to new post action`() = runTest {
        viewModel.obtainEvent(FeedEvent.NewPostClicked)

        assertEquals(FeedAction.NavigateToNewPost, viewModel.action.first())
    }

    @Test
    fun `post liked toggles like in state when request succeeds`() = runTest {
        val post = createPost(id = "1", isLiked = false, likeCount = 3)
        `when`(getFeedUseCase.execute()).thenReturn(Result.success(listOf(post)))
        `when`(likePostUseCase.execute("1")).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(FeedEvent.LoadPosts)
        advanceUntilIdle()
        viewModel.obtainEvent(FeedEvent.PostLiked("1"))
        advanceUntilIdle()

        val state = viewModel.state.first() as FeedState.Main
        assertEquals(true, state.posts.first().isLikedByCurrentUser)
        assertEquals(4, state.posts.first().likeCount)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        viewModel.obtainEvent(FeedEvent.NewPostClicked)
        viewModel.obtainEvent(FeedEvent.Clear)

        assertEquals(FeedState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createPost(
        id: String,
        isLiked: Boolean,
        likeCount: Int,
    ) = Post(
        id = id,
        authorId = "author",
        authorName = "Author",
        authorLogin = "author_login",
        authorAvatar = MediaItem("m1", MediaType.IMAGE, "url"),
        timestamp = 1L,
        content = "text",
        mediaItems = emptyList(),
        likeCount = likeCount,
        commentCount = 0,
        isLikedByCurrentUser = isLiked,
        fandoms = emptyList(),
    )
}

