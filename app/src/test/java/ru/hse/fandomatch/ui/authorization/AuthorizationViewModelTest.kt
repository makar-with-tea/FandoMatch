package ru.hse.fandomatch.ui.authorization

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
import org.mockito.Mockito.*
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.usecase.auth.LoginUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AuthorizationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AuthorizationViewModel
    private lateinit var loginUseCase: LoginUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mock(LoginUseCase::class.java)
        viewModel = AuthorizationViewModel(
            loginUseCase = loginUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher
        )
    }

    @Test
    fun `successful login triggers navigation action`() = runTest {
        // Arrange
        val login = "user"
        val password = "pass"
        `when`(loginUseCase.execute(login, password)).thenReturn(Unit)

        // Act
        viewModel.obtainEvent(AuthorizationEvent.LoginChanged(login))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged(password))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
        advanceUntilIdle()

        // Assert
        val action = viewModel.action.first()
        assertEquals(AuthorizationAction.NavigateToMatches, action)
    }

    @Test
    fun `login with empty fields sets errors`() = runTest {
        // Act
        viewModel.obtainEvent(AuthorizationEvent.LoginChanged(""))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged(""))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(AuthorizationState.AuthorizationError.EMPTY_LOGIN, state.loginError)
        assertEquals(AuthorizationState.AuthorizationError.EMPTY_PASSWORD, state.passwordError)
    }

    @Test
    fun `login with invalid credentials sets error`() = runTest {
        // Arrange
        val login = "user"
        val password = "wrong"
        `when`(loginUseCase.execute(login, password)).thenThrow(InvalidCredentialsException())

        // Act
        viewModel.obtainEvent(AuthorizationEvent.LoginChanged(login))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged(password))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(AuthorizationState.AuthorizationError.INVALID_CREDENTIALS, state.passwordError)
    }

    @Test
    fun `login with network error sets network error`() = runTest {
        // Arrange
        val login = "user"
        val password = "pass"
        `when`(loginUseCase.execute(login, password)).thenThrow(RuntimeException())

        // Act
        viewModel.obtainEvent(AuthorizationEvent.LoginChanged(login))
        viewModel.obtainEvent(AuthorizationEvent.PasswordChanged(password))
        viewModel.obtainEvent(AuthorizationEvent.LoginButtonClicked)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(AuthorizationState.AuthorizationError.NETWORK, state.passwordError)
        assertEquals(AuthorizationState.AuthorizationError.NETWORK, state.loginError)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        // Act
        viewModel.obtainEvent(AuthorizationEvent.Clear)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first()
        val action = viewModel.action.first()
        assertEquals(AuthorizationState.Main(), state)
        assertEquals(null, action)
    }

    @Test
    fun `show password button toggles visibility`() = runTest {
        // Arrange
        val initialState = viewModel.state.first() as AuthorizationState.Main
        val initialVisibility = initialState.passwordVisibility

        // Act
        viewModel.obtainEvent(AuthorizationEvent.ShowPasswordButtonClicked)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.first() as AuthorizationState.Main
        assertEquals(!initialVisibility, state.passwordVisibility)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
