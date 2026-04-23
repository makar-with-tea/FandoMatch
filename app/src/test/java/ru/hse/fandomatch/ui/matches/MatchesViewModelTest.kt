package ru.hse.fandomatch.ui.matches

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
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadSuggestedProfilesUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class MatchesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MatchesViewModel
    private lateinit var loadSuggestedProfilesUseCase: LoadSuggestedProfilesUseCase
    private lateinit var likeOrDislikeProfileUseCase: LikeOrDislikeProfileUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loadSuggestedProfilesUseCase = mock(LoadSuggestedProfilesUseCase::class.java)
        likeOrDislikeProfileUseCase = mock(LikeOrDislikeProfileUseCase::class.java)
        viewModel = MatchesViewModel(
            loadSuggestedProfilesUseCase = loadSuggestedProfilesUseCase,
            likeOrDislikeProfileUseCase = likeOrDislikeProfileUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load suggested profiles success sets main state`() = runTest {
        val profiles = listOf(profile("1"), profile("2"))
        `when`(loadSuggestedProfilesUseCase.execute(3)).thenReturn(Result.success(profiles))

        viewModel.obtainEvent(MatchesEvent.LoadSuggestedProfiles)
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is MatchesState.Main)
        state as MatchesState.Main
        assertEquals(profiles, state.profileStack)
        assertEquals(MatchesState.MatchesError.IDLE, state.error)
    }

    @Test
    fun `load suggested profiles failure sets network error`() = runTest {
        `when`(loadSuggestedProfilesUseCase.execute(3)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(MatchesEvent.LoadSuggestedProfiles)
        advanceUntilIdle()

        val state = viewModel.state.first() as MatchesState.Main
        assertEquals(MatchesState.MatchesError.NETWORK, state.error)
    }

    @Test
    fun `liked profile removes top card and keeps main state`() = runTest {
        `when`(loadSuggestedProfilesUseCase.execute(3)).thenReturn(
            Result.success(listOf(profile("1"), profile("2"), profile("3"))),
            Result.success(emptyList())
        )
        `when`(likeOrDislikeProfileUseCase.execute("1", true)).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(MatchesEvent.LoadSuggestedProfiles)
        advanceUntilIdle()
        viewModel.obtainEvent(MatchesEvent.LikedProfile("1"))
        advanceUntilIdle()

        val state = viewModel.state.first() as MatchesState.Main
        assertEquals(listOf("2", "3"), state.profileStack.map { it.id })
    }

    @Test
    fun `disliked profile removes top card`() = runTest {
        `when`(loadSuggestedProfilesUseCase.execute(3)).thenReturn(
            Result.success(listOf(profile("1"), profile("2"), profile("3"))),
            Result.success(emptyList())
        )
        `when`(likeOrDislikeProfileUseCase.execute("1", false)).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(MatchesEvent.LoadSuggestedProfiles)
        advanceUntilIdle()
        viewModel.obtainEvent(MatchesEvent.DislikedProfile("1"))
        advanceUntilIdle()

        val state = viewModel.state.first() as MatchesState.Main
        assertEquals(listOf("2", "3"), state.profileStack.map { it.id })
    }

    @Test
    fun `profile clicked emits navigate action`() = runTest {
        viewModel.obtainEvent(MatchesEvent.ProfileClicked("42"))

        assertEquals(MatchesState.Loading, viewModel.state.first())
        assertEquals(MatchesAction.NavigateToProfile("42"), viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        viewModel.obtainEvent(MatchesEvent.ProfileClicked("42"))
        viewModel.obtainEvent(MatchesEvent.Clear)
        advanceUntilIdle()

        assertEquals(MatchesState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun profile(id: String) = ProfileCard(
        id = id,
        fandoms = emptyList(),
        name = "Name$id",
        gender = Gender.NOT_SPECIFIED,
        age = 20,
        city = City("Москва", "Moscow"),
        compatibilityPercentage = 80,
    )
}

