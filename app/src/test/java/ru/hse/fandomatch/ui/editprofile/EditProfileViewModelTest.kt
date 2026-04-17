package ru.hse.fandomatch.ui.editprofile

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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.hse.fandomatch.data.mock.mockFandoms
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EditProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EditProfileViewModel(
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher
        )
    }

    @Test
    fun `initial state is Idle`() = runTest {
        assertTrue(viewModel.state.value is EditProfileState.Idle)
    }

    @Test
    fun `LoadProfileData sets Loading then Main state`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        assertTrue(viewModel.state.value is EditProfileState.Main)
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(mockUser.name, state.name)
        assertEquals(mockUser.login, state.login)
    }

    @Test
    fun `NameChanged with invalid length sets error`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.NameChanged("A"))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.NAME_LENGTH, state.nameError)
    }

    @Test
    fun `NameChanged with invalid content sets error`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.NameChanged("123456"))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.NAME_CONTENT, state.nameError)
    }

    @Test
    fun `NameChanged with valid name clears error`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.NameChanged("Иван Иванов"))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.IDLE, state.nameError)
        assertEquals("Иван Иванов", state.name)
    }

    @Test
    fun `DescriptionChanged with too long description sets error`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        val longDesc = "a".repeat(2000)
        viewModel.obtainEvent(EditProfileEvent.DescriptionChanged(longDesc))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.DESCRIPTION_LENGTH, state.descriptionError)
    }

    @Test
    fun `DescriptionChanged with valid description clears error`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.DescriptionChanged("Hello!"))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.IDLE, state.descriptionError)
        assertEquals("Hello!", state.description)
    }

    @Test
    fun `CityChanged with unknown city sets error`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.CityChanged("Atlantis"))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.CITY_NOT_FOUND, state.cityError)
    }

    @Test
    fun `CityChanged with known city clears error`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        val city = "Москва"
        viewModel.obtainEvent(EditProfileEvent.CityChanged(city))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.IDLE, state.cityError)
        assertEquals(city, state.city)
    }

    @Test
    fun `FandomAdded adds fandom to list`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        val fandom = Fandom(100, "TestFandom", FandomCategory.OTHER)
        viewModel.obtainEvent(EditProfileEvent.FandomAdded(fandom))
        val state = viewModel.state.value as EditProfileState.Main
        assertTrue(state.fandoms.contains(fandom))
    }

    @Test
    fun `FandomRemoved removes fandom from list`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        val fandom = (viewModel.state.value as EditProfileState.Main).fandoms.first()
        viewModel.obtainEvent(EditProfileEvent.FandomRemoved(fandom))
        val state = viewModel.state.value as EditProfileState.Main
        assertFalse(state.fandoms.contains(fandom))
    }

    @Test
    fun `FandomSearched with query sets foundFandoms and areFandomsLoading`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.FandomSearched("One"))
        advanceUntilIdle()
        val state = viewModel.state.value as EditProfileState.Main
        assertTrue(state.foundFandoms.all { it.name.contains("One", ignoreCase = true) })
        assertFalse(state.areFandomsLoading)
    }

    @Test
    fun `FandomSearched with blank query clears foundFandoms`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.FandomSearched(""))
        val state = viewModel.state.value as EditProfileState.Main
        assertTrue(state.foundFandoms.isEmpty())
    }

    @Test
    fun `AddFandomButtonClicked sets NavigateToAddFandom action`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.AddFandomButtonClicked)
        assertEquals(EditProfileAction.NavigateToAddFandom, viewModel.action.value)
    }

    @Test
    fun `SaveButtonClicked sets Loading then NavigateToMyProfile action`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.SaveButtonClicked)
        advanceUntilIdle()
        assertEquals(EditProfileAction.NavigateToMyProfile, viewModel.action.value)
    }

    @Test
    fun `Clear resets state and action`() = runTest {
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.AddFandomButtonClicked)
        viewModel.obtainEvent(EditProfileEvent.Clear)
        assertTrue(viewModel.state.value is EditProfileState.Idle)
        assertNull(viewModel.action.value)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
