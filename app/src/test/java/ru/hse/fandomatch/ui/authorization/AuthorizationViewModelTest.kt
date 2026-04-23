package ru.hse.fandomatch.ui.authorization

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
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
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.usecase.auth.LoginUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AuthorizationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AuthorizationViewModel
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var testScheduler: TestCoroutineScheduler
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testScheduler = TestCoroutineScheduler()
        testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mock(LoginUseCase::class.java)
        viewModel = AuthorizationViewModel(
            loginUseCase = loginUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `login changed updates login and error`() = runTest(testScheduler) {
        viewModel.obtainEvent(AuthorizationEvent.LoginChanged("user"))

        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals("user", state.login)
        assertEquals(AuthorizationState.AuthorizationError.IDLE, state.loginError)
    }

    @Test
    fun `password changed updates password and error`() = runTest(testScheduler) {
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged("pass"))

        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals("pass", state.password)
        assertEquals(AuthorizationState.AuthorizationError.IDLE, state.passwordError)
    }

    @Test
    fun `show password toggles visibility`() = runTest(testScheduler) {
        val initial = (viewModel.state.first() as AuthorizationState.Main).passwordVisibility

        viewModel.obtainEvent(AuthorizationEvent.ShowPasswordButtonClicked)

        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(!initial, state.passwordVisibility)
    }

    @Test
    fun `forgot password button emits navigation action`() = runTest(testScheduler) {
        viewModel.obtainEvent(AuthorizationEvent.ForgotPasswordButtonClicked)

        assertEquals(AuthorizationAction.NavigateToPasswordRecovery, viewModel.action.first())
    }

    @Test
    fun `login button with empty fields sets validation errors`() = runTest(testScheduler) {
        viewModel.obtainEvent(AuthorizationEvent.LoginChanged(""))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged(""))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)

        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(AuthorizationState.AuthorizationError.EMPTY_LOGIN, state.loginError)
        assertEquals(AuthorizationState.AuthorizationError.EMPTY_PASSWORD, state.passwordError)
    }

    @Test
    fun `login button success emits navigate to matches action`() = runTest(testScheduler) {
        `when`(loginUseCase.execute("user", "pass")).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(AuthorizationEvent.LoginChanged("user"))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged("pass"))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
        advanceUntilIdle()

        assertEquals(AuthorizationAction.NavigateToMatches, viewModel.action.first())
    }

    @Test
    fun `login button invalid credentials sets invalid credentials error`() = runTest(testScheduler) {
        `when`(loginUseCase.execute("user", "wrong")).thenReturn(
            Result.failure(InvalidCredentialsException())
        )

        viewModel.obtainEvent(AuthorizationEvent.LoginChanged("user"))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged("wrong"))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
        advanceUntilIdle()

        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(AuthorizationState.AuthorizationError.INVALID_CREDENTIALS, state.passwordError)
        assertEquals(AuthorizationState.AuthorizationError.IDLE, state.loginError)
    }

    @Test
    fun `login button network error sets network errors`() = runTest(testScheduler) {
        `when`(loginUseCase.execute("user", "pass")).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(AuthorizationEvent.LoginChanged("user"))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged("pass"))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
        advanceUntilIdle()

        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(AuthorizationState.AuthorizationError.NETWORK, state.loginError)
        assertEquals(AuthorizationState.AuthorizationError.NETWORK, state.passwordError)
    }

    @Test
    fun `clear resets state and action`() = runTest(testScheduler) {
        viewModel.obtainEvent(AuthorizationEvent.LoginChanged("user"))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged("pass"))
        viewModel.obtainEvent(AuthorizationEvent.Clear)

        assertEquals(AuthorizationState.Main(), viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
