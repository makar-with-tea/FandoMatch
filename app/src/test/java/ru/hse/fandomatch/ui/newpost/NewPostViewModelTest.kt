package ru.hse.fandomatch.ui.newpost

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
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.fandoms.GetFandomsByQueryUseCase
import ru.hse.fandomatch.domain.usecase.posts.CreatePostUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class NewPostViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: NewPostViewModel
    private lateinit var createPostUseCase: CreatePostUseCase
    private lateinit var uploadMediaUseCase: UploadMediaUseCase
    private lateinit var getFandomsByQueryUseCase: GetFandomsByQueryUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createPostUseCase = mock(CreatePostUseCase::class.java)
        uploadMediaUseCase = mock(UploadMediaUseCase::class.java)
        getFandomsByQueryUseCase = mock(GetFandomsByQueryUseCase::class.java)

        viewModel = NewPostViewModel(
            createPostUseCase = createPostUseCase,
            uploadMediaUseCase = uploadMediaUseCase,
            getFandomsByQueryUseCase = getFandomsByQueryUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `content changed updates content and error`() = runTest {
        viewModel.obtainEvent(NewPostEvent.ContentChanged("hello"))

        val state = viewModel.state.first() as NewPostState.Main
        assertEquals("hello", state.content)
        assertEquals(NewPostState.NewPostError.IDLE, state.contentError)
    }

    @Test
    fun `attachments changed truncates by max allowed`() = runTest {
        val attachments = List(12) { byteArrayOf(it.toByte()) to MediaType.IMAGE }

        viewModel.obtainEvent(NewPostEvent.AttachmentsChanged(attachments))

        val state = viewModel.state.first() as NewPostState.Main
        assertEquals(10, state.attachedFilesWithTypes.size)
    }

    @Test
    fun `fandom added and removed updates list`() = runTest {
        val fandom = fandom("f1")

        viewModel.obtainEvent(NewPostEvent.FandomAdded(fandom))
        var state = viewModel.state.first() as NewPostState.Main
        assertTrue(fandom in state.fandoms)

        viewModel.obtainEvent(NewPostEvent.FandomRemoved(fandom))
        state = viewModel.state.first() as NewPostState.Main
        assertTrue(fandom !in state.fandoms)
    }

    @Test
    fun `fandom searched updates found fandoms`() = runTest {
        val found = listOf(fandom("f2"))
        `when`(getFandomsByQueryUseCase.execute("naruto")).thenReturn(Result.success(found))

        viewModel.obtainEvent(NewPostEvent.FandomSearched("naruto"))
        advanceUntilIdle()

        val state = viewModel.state.first() as NewPostState.Main
        assertEquals(found, state.foundFandoms)
        assertEquals(false, state.areFandomsLoading)
    }

    @Test
    fun `add fandom clicked emits navigate to add fandom action`() = runTest {
        viewModel.obtainEvent(NewPostEvent.AddFandomClicked)

        assertEquals(NewPostAction.NavigateToAddFandom, viewModel.action.first())
    }

    @Test
    fun `post button clicked success emits navigate back action`() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val fandom = fandom("f1")
        `when`(uploadMediaUseCase.execute(bytes, MediaType.IMAGE)).thenReturn(Result.success("media-id"))
        `when`(
            createPostUseCase.execute(
                content = "post",
                mediaIdsWithTypes = listOf("media-id" to MediaType.IMAGE),
                fandomIds = listOf("f1"),
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(NewPostEvent.ContentChanged("post"))
        viewModel.obtainEvent(NewPostEvent.FandomAdded(fandom))
        viewModel.obtainEvent(NewPostEvent.AttachmentsChanged(listOf(bytes to MediaType.IMAGE)))
        viewModel.obtainEvent(NewPostEvent.PostButtonClicked)
        advanceUntilIdle()

        assertEquals(NewPostAction.NavigateToPreviousScreen, viewModel.action.first())
    }

    @Test
    fun `post button clicked failure emits error toast action and toast shown clears it`() = runTest {
        `when`(
            createPostUseCase.execute(
                content = "post",
                mediaIdsWithTypes = emptyList(),
                fandomIds = emptyList(),
            )
        ).thenReturn(Result.failure(RuntimeException("network")))

        viewModel.obtainEvent(NewPostEvent.ContentChanged("post"))
        viewModel.obtainEvent(NewPostEvent.PostButtonClicked)
        advanceUntilIdle()

        assertEquals(NewPostAction.ShowErrorToast, viewModel.action.first())

        viewModel.obtainEvent(NewPostEvent.ToastShown)
        assertEquals(null, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        viewModel.obtainEvent(NewPostEvent.AddFandomClicked)
        viewModel.obtainEvent(NewPostEvent.Clear)

        assertEquals(NewPostState.Main(), viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fandom(id: String) = Fandom(
        id = id,
        name = "Fandom$id",
        category = FandomCategory.BOOKS,
    )
}

