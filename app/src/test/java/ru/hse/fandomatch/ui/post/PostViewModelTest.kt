package ru.hse.fandomatch.ui.post

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
import ru.hse.fandomatch.domain.model.Comment
import ru.hse.fandomatch.domain.model.FullPost
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.usecase.media.DownloadMediaToGalleryUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetFullPostUseCase
import ru.hse.fandomatch.domain.usecase.posts.LikePostUseCase
import ru.hse.fandomatch.domain.usecase.posts.SendCommentUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PostViewModel
    private lateinit var getFullPostUseCase: GetFullPostUseCase
    private lateinit var sendCommentUseCase: SendCommentUseCase
    private lateinit var getUserUseCase: GetUserUseCase
    private lateinit var likePostUseCase: LikePostUseCase
    private lateinit var downloadMediaToGalleryUseCase: DownloadMediaToGalleryUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getFullPostUseCase = mock(GetFullPostUseCase::class.java)
        sendCommentUseCase = mock(SendCommentUseCase::class.java)
        getUserUseCase = mock(GetUserUseCase::class.java)
        likePostUseCase = mock(LikePostUseCase::class.java)
        downloadMediaToGalleryUseCase = mock(DownloadMediaToGalleryUseCase::class.java)
        viewModel = PostViewModel(
            getFullPostUseCase = getFullPostUseCase,
            sendCommentUseCase = sendCommentUseCase,
            getUserUseCase = getUserUseCase,
            likePostUseCase = likePostUseCase,
            downloadMediaToGalleryUseCase = downloadMediaToGalleryUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load post null sets error state`() = runTest {
        viewModel.obtainEvent(PostEvent.LoadPost(null))

        assertEquals(PostState.Error, viewModel.state.first())
    }

    @Test
    fun `load post success sets main state`() = runTest {
        val fullPost = fullPost(isLiked = false, likeCount = 5)
        `when`(getFullPostUseCase.execute("post-1")).thenReturn(Result.success(fullPost))

        viewModel.obtainEvent(PostEvent.LoadPost("post-1"))
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is PostState.Main)
        assertEquals(fullPost, (state as PostState.Main).fullPost)
    }

    @Test
    fun `load post failure sets error state`() = runTest {
        `when`(getFullPostUseCase.execute("post-1")).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(PostEvent.LoadPost("post-1"))
        advanceUntilIdle()

        assertEquals(PostState.Error, viewModel.state.first())
    }

    @Test
    fun `update comment draft updates state`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PostEvent.UpdateCommentDraft("hello"))

        val state = viewModel.state.first() as PostState.Main
        assertEquals("hello", state.commentDraft)
    }

    @Test
    fun `send comment success clears draft and appends comment`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PostEvent.UpdateCommentDraft("nice post"))
        `when`(sendCommentUseCase.execute("post-1", "nice post", 123L)).thenReturn(Result.success(Unit))
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.success(currentUser()))

        viewModel.obtainEvent(PostEvent.SendComment)
        advanceUntilIdle()

        val state = viewModel.state.first() as PostState.Main
        assertEquals("", state.commentDraft)
        assertEquals(1, state.fullPost.comments.size)
        assertEquals("nice post", state.fullPost.comments.first().content)
    }

    @Test
    fun `send comment failure keeps draft`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PostEvent.UpdateCommentDraft("nice post"))
        `when`(sendCommentUseCase.execute("post-1", "nice post", 123L)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(PostEvent.SendComment)
        advanceUntilIdle()

        val state = viewModel.state.first() as PostState.Main
        assertEquals("nice post", state.commentDraft)
    }

    @Test
    fun `profile clicked emits navigation action`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PostEvent.ProfileClicked)

        assertEquals(PostAction.GoToProfile("author-1"), viewModel.action.first())
    }

    @Test
    fun `like clicked toggles like in state`() = runTest {
        loadMainState()
        advanceUntilIdle()

        `when`(likePostUseCase.execute("post-1")).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(PostEvent.LikeClicked)
        advanceUntilIdle()

        val state = viewModel.state.first() as PostState.Main
        assertEquals(true, state.fullPost.post.isLikedByCurrentUser)
        assertEquals(6, state.fullPost.post.likeCount)
    }

    @Test
    fun `download media item success emits success toast action`() = runTest {
        `when`(downloadMediaToGalleryUseCase.execute("url", MediaType.IMAGE)).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(PostEvent.DownloadMediaItem(MediaItem("m1", MediaType.IMAGE, "url")))
        advanceUntilIdle()

        assertEquals(PostAction.ShowSuccessDownloadToast, viewModel.action.first())
    }

    @Test
    fun `download media item failure emits error toast action and toast shown clears it`() = runTest {
        `when`(downloadMediaToGalleryUseCase.execute("url", MediaType.IMAGE)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(PostEvent.DownloadMediaItem(MediaItem("m1", MediaType.IMAGE, "url")))
        advanceUntilIdle()

        assertEquals(PostAction.ShowErrorDownloadToast, viewModel.action.first())

        viewModel.obtainEvent(PostEvent.ToastShown)
        assertEquals(null, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PostEvent.ToastShown)
        viewModel.obtainEvent(PostEvent.Clear)

        assertEquals(PostState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun loadMainState() {
        `when`(getFullPostUseCase.execute("post-1")).thenReturn(Result.success(fullPost(isLiked = false, likeCount = 5)))
        viewModel.obtainEvent(PostEvent.LoadPost("post-1"))
    }

    private fun currentUser() = User(
        id = "current-user",
        fandoms = emptyList(),
        name = "Me",
        gender = ru.hse.fandomatch.domain.model.Gender.FEMALE,
        age = 20,
        avatar = MediaItem("avatar", MediaType.IMAGE, "avatar-url"),
        background = null,
        city = null,
        profileType = ProfileType.Own(login = "me_login", email = "me@mail.com"),
    )

    private fun fullPost(isLiked: Boolean, likeCount: Int) = FullPost(
        post = Post(
            id = "post-1",
            authorId = "author-1",
            authorName = "Author",
            authorLogin = "author_login",
            authorAvatar = MediaItem("avatar", MediaType.IMAGE, "avatar-url"),
            timestamp = 1L,
            content = "hello",
            mediaItems = listOf(MediaItem("m1", MediaType.IMAGE, "url")),
            likeCount = likeCount,
            commentCount = 0,
            isLikedByCurrentUser = isLiked,
            fandoms = emptyList(),
        ),
        comments = emptyList(),
    )
}

