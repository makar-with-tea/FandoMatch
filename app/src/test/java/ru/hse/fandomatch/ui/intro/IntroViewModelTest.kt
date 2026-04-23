package ru.hse.fandomatch.ui.intro

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
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import ru.hse.fandomatch.domain.usecase.auth.GetPastLoginUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class IntroViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: IntroViewModel
    private lateinit var getPastLoginUseCase: GetPastLoginUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getPastLoginUseCase = mock(GetPastLoginUseCase::class.java)
        viewModel = IntroViewModel(
            getPastLoginUseCase = getPastLoginUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `go to login emits navigate to login action`() = runTest {
        viewModel.obtainEvent(IntroEvent.GoToLoginButtonClicked)

        assertEquals(IntroState.Main, viewModel.state.first())
        assertEquals(IntroAction.NavigateToLogin, viewModel.action.first())
    }

    @Test
    fun `go to registration emits navigate to registration action`() = runTest {
        viewModel.obtainEvent(IntroEvent.GoToRegistrationButtonClicked)

        assertEquals(IntroState.Main, viewModel.state.first())
        assertEquals(IntroAction.NavigateToRegistration, viewModel.action.first())
    }

    @Test
    fun `check past login with stored user navigates to matches`() = runTest {
        `when`(getPastLoginUseCase.execute()).thenReturn("user-id")

        viewModel.obtainEvent(IntroEvent.CheckPastLogin)
        advanceUntilIdle()

        assertEquals(IntroAction.NavigateToMatches, viewModel.action.first())
    }

    @Test
    fun `check past login without stored user shows main state`() = runTest {
        `when`(getPastLoginUseCase.execute()).thenReturn(null)

        viewModel.obtainEvent(IntroEvent.CheckPastLogin)
        advanceUntilIdle()

        assertEquals(IntroState.Main, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        viewModel.obtainEvent(IntroEvent.GoToLoginButtonClicked)
        viewModel.obtainEvent(IntroEvent.Clear)
        advanceUntilIdle()

        assertEquals(IntroState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

