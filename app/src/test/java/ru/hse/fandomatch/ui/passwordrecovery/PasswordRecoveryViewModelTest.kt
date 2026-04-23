package ru.hse.fandomatch.ui.passwordrecovery

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
import ru.hse.fandomatch.domain.usecase.auth.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.ResetPasswordUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordRecoveryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PasswordRecoveryViewModel
    private lateinit var getVerificationCodeUseCase: GetVerificationCodeUseCase
    private lateinit var resetPasswordUseCase: ResetPasswordUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getVerificationCodeUseCase = mock(GetVerificationCodeUseCase::class.java)
        resetPasswordUseCase = mock(ResetPasswordUseCase::class.java)
        viewModel = PasswordRecoveryViewModel(
            getVerificationCodeUseCase = getVerificationCodeUseCase,
            resetPasswordUseCase = resetPasswordUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `email changed updates email and error`() = runTest {
        viewModel.obtainEvent(PasswordRecoveryEvent.EmailChanged("user@mail.com"))

        val state = viewModel.state.first() as PasswordRecoveryState.Email
        assertEquals("user@mail.com", state.email)
        assertEquals(PasswordRecoveryState.PasswordRecoveryError.IDLE, state.emailError)
    }

    @Test
    fun `send code with invalid email sets error`() = runTest {
        viewModel.obtainEvent(PasswordRecoveryEvent.EmailChanged("bad-email"))
        viewModel.obtainEvent(PasswordRecoveryEvent.SendCodeClicked)

        val state = viewModel.state.first() as PasswordRecoveryState.Email
        assertEquals(PasswordRecoveryState.PasswordRecoveryError.EMAIL_CONTENT, state.emailError)
    }

    @Test
    fun `send code success switches to main state`() = runTest {
        `when`(getVerificationCodeUseCase.execute("user@mail.com")).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(PasswordRecoveryEvent.EmailChanged("user@mail.com"))
        viewModel.obtainEvent(PasswordRecoveryEvent.SendCodeClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.first() is PasswordRecoveryState.Main)
    }

    @Test
    fun `send code failure sets network error`() = runTest {
        `when`(getVerificationCodeUseCase.execute("user@mail.com")).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(PasswordRecoveryEvent.EmailChanged("user@mail.com"))
        viewModel.obtainEvent(PasswordRecoveryEvent.SendCodeClicked)
        advanceUntilIdle()

        val state = viewModel.state.first() as PasswordRecoveryState.Email
        assertEquals(PasswordRecoveryState.PasswordRecoveryError.NETWORK, state.emailError)
    }

    @Test
    fun `new password changed validates content`() = runTest {
        goToMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PasswordRecoveryEvent.NewPasswordChanged("short"))

        val state = viewModel.state.first() as PasswordRecoveryState.Main
        assertEquals(PasswordRecoveryState.PasswordRecoveryError.PASSWORD_LENGTH, state.newPasswordError)
    }

    @Test
    fun `repeat password changed validates mismatch`() = runTest {
        goToMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PasswordRecoveryEvent.NewPasswordChanged("Password123!"))
        viewModel.obtainEvent(PasswordRecoveryEvent.RepeatNewPasswordChanged("Other123!"))

        val state = viewModel.state.first() as PasswordRecoveryState.Main
        assertEquals(PasswordRecoveryState.PasswordRecoveryError.PASSWORD_MISMATCH, state.repeatNewPasswordError)
    }

    @Test
    fun `toggle password visibility events change flags`() = runTest {
        goToMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(PasswordRecoveryEvent.ToggleNewPasswordVisibility)
        viewModel.obtainEvent(PasswordRecoveryEvent.ToggleRepeatNewPasswordVisibility)

        val state = viewModel.state.first() as PasswordRecoveryState.Main
        assertTrue(state.newPasswordVisibility)
        assertTrue(state.repeatNewPasswordVisibility)
    }

    @Test
    fun `save password with invalid code shows code error`() = runTest {
        goToMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(PasswordRecoveryEvent.NewPasswordChanged("Password123!"))
        viewModel.obtainEvent(PasswordRecoveryEvent.RepeatNewPasswordChanged("Password123!"))

        viewModel.obtainEvent(PasswordRecoveryEvent.SavePasswordClicked(""))

        val state = viewModel.state.first() as PasswordRecoveryState.Main
        assertEquals(PasswordRecoveryState.PasswordRecoveryError.EMPTY_CODE, state.codeError)
    }

    @Test
    fun `save password with wrong code sets invalid code`() = runTest {
        goToMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(PasswordRecoveryEvent.NewPasswordChanged("Password123!"))
        viewModel.obtainEvent(PasswordRecoveryEvent.RepeatNewPasswordChanged("Password123!"))
        `when`(resetPasswordUseCase.execute("1234", "Password123!")).thenReturn(
            Result.failure(IllegalArgumentException("invalid code"))
        )

        viewModel.obtainEvent(PasswordRecoveryEvent.SavePasswordClicked("1234"))
        advanceUntilIdle()

        val state = viewModel.state.first() as PasswordRecoveryState.Main
        assertEquals(PasswordRecoveryState.PasswordRecoveryError.INVALID_CODE, state.codeError)
    }

    @Test
    fun `save password success emits navigation action`() = runTest {
        goToMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(PasswordRecoveryEvent.NewPasswordChanged("Password123!"))
        viewModel.obtainEvent(PasswordRecoveryEvent.RepeatNewPasswordChanged("Password123!"))
        `when`(resetPasswordUseCase.execute("1234", "Password123!")).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(PasswordRecoveryEvent.SavePasswordClicked("1234"))
        advanceUntilIdle()

        assertEquals(PasswordRecoveryAction.NavigateToAuthorization, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        goToMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(PasswordRecoveryEvent.Clear)

        assertEquals(PasswordRecoveryState.Email(), viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun goToMainState() {
        viewModel.obtainEvent(PasswordRecoveryEvent.EmailChanged("user@mail.com"))
        `when`(getVerificationCodeUseCase.execute("user@mail.com")).thenReturn(Result.success(Unit))
        viewModel.obtainEvent(PasswordRecoveryEvent.SendCodeClicked)
    }
}
