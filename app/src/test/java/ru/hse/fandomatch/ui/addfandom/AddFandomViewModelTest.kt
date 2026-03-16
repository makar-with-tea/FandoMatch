package ru.hse.fandomatch.ui.addfandom

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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.hse.fandomatch.domain.model.FandomCategory

@OptIn(ExperimentalCoroutinesApi::class)
class AddFandomViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AddFandomViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddFandomViewModel(
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher
        )
    }

    @Test
    fun `nameChanged with valid name sets state and enables button`() = runTest {
        // Act
        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals("ValidName", state.name)
        assertEquals(AddFandomState.AddFandomError.IDLE, state.nameError)
        assertEquals(true, state.isButtonAvailable)
    }

    @Test
    fun `nameChanged with invalid name sets error and disables button`() = runTest {
        // Act
        viewModel.obtainEvent(AddFandomEvent.NameChanged(""))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals("", state.name)
        assertEquals(AddFandomState.AddFandomError.NAME_LENGTH, state.nameError)
        assertEquals(false, state.isButtonAvailable)
    }

    @Test
    fun `categoryChanged updates category in state`() = runTest {
        // Act
        viewModel.obtainEvent(AddFandomEvent.CategoryChanged(FandomCategory.BOOKS))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals(FandomCategory.BOOKS, state.category)
    }

    @Test
    fun `descriptionChanged with valid description updates state`() = runTest {
        // Act
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("Some description"))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals("Some description", state.description)
        assertEquals(AddFandomState.AddFandomError.IDLE, state.nameError)
        assertEquals(true, state.isButtonAvailable)
    }

    @Test
    fun `descriptionChanged with invalid description sets error and disables button`() = runTest {
        // Act
        val longDescription = "a".repeat(4000)
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged(longDescription))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals(longDescription, state.description)
        assertEquals(AddFandomState.AddFandomError.DESCRIPTION_LENGTH, state.nameError)
        assertEquals(false, state.isButtonAvailable)
    }

    @Test
    fun `send with valid data triggers success action`() = runTest {
        // Arrange
        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("Valid description"))
        advanceUntilIdle()

        // Act
        viewModel.obtainEvent(AddFandomEvent.SendButtonClicked)
        advanceUntilIdle()

        // Assert
        val action = viewModel.action.first()
        assertEquals(AddFandomAction.ShowSuccessToastAndGoBack, action)
    }

    @Test
    fun `send with invalid name sets error and does not trigger action`() = runTest {
        // Arrange
        viewModel.obtainEvent(AddFandomEvent.NameChanged(""))
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("Valid description"))
        advanceUntilIdle()

        // Act
        viewModel.obtainEvent(AddFandomEvent.SendButtonClicked)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals(AddFandomState.AddFandomError.NAME_LENGTH, state.nameError)
        assertEquals(false, state.isButtonAvailable)
        val action = viewModel.action.first()
        assertEquals(null, action)
    }

    @Test
    fun `send with invalid description sets error and does not trigger action`() = runTest {
        // Arrange
        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        val longDescription = "a".repeat(4000)
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged(longDescription))
        advanceUntilIdle()

        // Act
        viewModel.obtainEvent(AddFandomEvent.SendButtonClicked)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AddFandomState.Main
        assertEquals(AddFandomState.AddFandomError.DESCRIPTION_LENGTH, state.descriptionError)
        assertEquals(false, state.isButtonAvailable)
        val action = viewModel.action.first()
        assertEquals(null, action)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        // Arrange
        viewModel.obtainEvent(AddFandomEvent.NameChanged("ValidName"))
        viewModel.obtainEvent(AddFandomEvent.CategoryChanged(FandomCategory.BOOKS))
        viewModel.obtainEvent(AddFandomEvent.DescriptionChanged("desc"))
        advanceUntilIdle()

        // Act
        viewModel.obtainEvent(AddFandomEvent.Clear)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        val action = viewModel.action.first()
        assertEquals(AddFandomState.Main(), state)
        assertEquals(null, action)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
