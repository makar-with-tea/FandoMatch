package ru.hse.fandomatch.ui.profile

import android.util.Log
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
import org.junit.runner.RunWith
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.User
import java.time.LocalDate


@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher
        )
    }

    @Test
    fun `load own profile successfully`() = runTest {
        // Act
        viewModel.obtainEvent(ProfileEvent.LoadProfile(mockUser.id))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        assert(state is ProfileState.Main)
        assertEquals(mockUser.id, (state as ProfileState.Main).id)
    }

    @Test
    fun `load profile with invalid id returns error`() = runTest {
        // Act
        viewModel.obtainEvent(ProfileEvent.LoadProfile(-1L))
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        assertEquals(ProfileState.Error(ProfileState.ProfileError.NO_USER), state)
    }

    @Test
    fun `edit profile button sets action`() = runTest {
        // Act
        viewModel.obtainEvent(ProfileEvent.EditProfileButtonClicked)
        advanceUntilIdle()

        // Assert
        val action = viewModel.action.first()
        assertEquals(ProfileAction.GoToEditProfile, action)
    }

    @Test
    fun `settings button sets action`() = runTest {
        // Act
        viewModel.obtainEvent(ProfileEvent.SettingsButtonClicked)
        advanceUntilIdle()

        // Assert
        val action = viewModel.action.first()
        assertEquals(ProfileAction.GoToSettings, action)
    }

    @Test
    fun `message button sets action`() = runTest {
        // Act
        viewModel.obtainEvent(ProfileEvent.MessageButtonClicked(mockUser.id))
        advanceUntilIdle()

        // Assert
        val action = viewModel.action.first()
        assertEquals(ProfileAction.GoToMessages(mockUser.id), action)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        // Act
        viewModel.obtainEvent(ProfileEvent.Clear)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        val action = viewModel.action.first()
        assertEquals(ProfileState.Idle, state)
        assertEquals(null, action)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    companion object {
        // Mock user data for testing
        private val fandoms = listOf(
            Fandom(
                id = 1,
                name = "One Piece",
                category = FandomCategory.ANIME_MANGA
            ),
            Fandom(
                id = 2,
                name = "Harry Potter",
                category = FandomCategory.BOOKS
            ),
            Fandom(
                id = 3,
                name = "No-Enor",
                category = FandomCategory.OTHER
            )
        )
        private val mockUser = User(
            id = 1L,
            name = "John Doe",
            email = "johndoe@mail.com",
            login = "johndoe",
            phone = null,
            fandoms = fandoms,
            description = "Just a test user",
            gender = Gender.MALE,
            birthDate = LocalDate.now().minusYears(25),
            avatarUrl = "https://example.com/avatar.jpg",
            backgroundUrl = "https://example.com/background.jpg",
            city = City(
                nameRussian = "Москва",
                nameEnglish = "Moscow"
            )
        )
    }
}
