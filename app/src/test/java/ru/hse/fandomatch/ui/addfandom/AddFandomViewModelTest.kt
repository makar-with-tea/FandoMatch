package ru.hse.fandomatch.ui.addfandom

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.usecase.fandoms.RequestNewFandomUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AddFandomViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AddFandomViewModel
    private lateinit var requestNewFandomUseCase: RequestNewFandomUseCase
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        requestNewFandomUseCase = mock(RequestNewFandomUseCase::class.java)
        viewModel = AddFandomViewModel(requestNewFandomUseCase, testDispatcher, testDispatcher)
    }

    @Test
    fun `name changed with valid value updates state`() = runTest {
        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))

        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals("ValidName", state.name)
        assertEquals(AddFandomState.AddFandomError.IDLE, state.nameError)
        assertEquals(true, state.isButtonAvailable)
    }

    @Test
    fun `name changed with invalid value disables button`() = runTest {
        viewModel.obtainEvent(AddFandomEvent.NameChanged(""))

        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals("", state.name)
        assertEquals(AddFandomState.AddFandomError.NAME_LENGTH, state.nameError)
        assertEquals(false, state.isButtonAvailable)
    }

    @Test
    fun `category changed updates category`() = runTest {
        viewModel.obtainEvent(AddFandomEvent.CategoryChanged(FandomCategory.BOOKS))

        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals(FandomCategory.BOOKS, state.category)
    }

    @Test
    fun `description changed with valid value updates state`() = runTest {
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("Some description"))

        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals("Some description", state.description)
        assertEquals(AddFandomState.AddFandomError.IDLE, state.descriptionError)
    }

    @Test
    fun `description changed with invalid value disables button`() = runTest {
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("a".repeat(4000)))

        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals(AddFandomState.AddFandomError.DESCRIPTION_LENGTH, state.descriptionError)
        assertEquals(false, state.isButtonAvailable)
    }

    @Test
    fun `send button success emits go back action`() = runTest {
        `when`(
            requestNewFandomUseCase.execute(
                name = "ValidName",
                category = FandomCategory.BOOKS,
                description = "Valid description",
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        viewModel.obtainEvent(AddFandomEvent.CategoryChanged(FandomCategory.BOOKS))
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("Valid description"))
        viewModel.obtainEvent(AddFandomEvent.SendButtonClicked)
        advanceUntilIdle()

        assertEquals(AddFandomAction.ShowSuccessToastAndGoBack, viewModel.action.first())
    }

    @Test
    fun `send button failure emits network error action`() = runTest {
        `when`(
            requestNewFandomUseCase.execute(
                name = "ValidName",
                category = FandomCategory.BOOKS,
                description = "Valid description",
            )
        ).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        viewModel.obtainEvent(AddFandomEvent.CategoryChanged(FandomCategory.BOOKS))
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("Valid description"))
        viewModel.obtainEvent(AddFandomEvent.SendButtonClicked)
        advanceUntilIdle()

        assertEquals(AddFandomAction.ShowNetworkErrorToast, viewModel.action.first())
    }

    @Test
    fun `toast shown clears action`() = runTest {
        `when`(
            requestNewFandomUseCase.execute(
                name = "ValidName",
                category = FandomCategory.BOOKS,
                description = "Valid description",
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("Valid description"))
        viewModel.obtainEvent(AddFandomEvent.SendButtonClicked)
        advanceUntilIdle()
        viewModel.obtainEvent(AddFandomEvent.ToastShown)

        assertEquals(null, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        viewModel.obtainEvent(AddFandomEvent.CategoryChanged(FandomCategory.BOOKS))
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("desc"))
        viewModel.obtainEvent(AddFandomEvent.Clear)

        assertEquals(AddFandomState.Main(), viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
