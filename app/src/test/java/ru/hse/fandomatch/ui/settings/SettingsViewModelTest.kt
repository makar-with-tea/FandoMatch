package ru.hse.fandomatch.ui.settings

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
import ru.hse.fandomatch.domain.exception.InvalidCredentialsException
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.model.UserPreferences
import ru.hse.fandomatch.domain.usecase.auth.ChangeEmailUseCase
import ru.hse.fandomatch.domain.usecase.auth.ChangePasswordUseCase
import ru.hse.fandomatch.domain.usecase.auth.CheckVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.DeleteAccountUseCase
import ru.hse.fandomatch.domain.usecase.auth.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.LogoutUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserPreferencesUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase
import ru.hse.fandomatch.domain.usecase.user.UpdateUserPreferencesUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var getUserUseCase: GetUserUseCase
    private lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase
    private lateinit var updateUserPreferencesUseCase: UpdateUserPreferencesUseCase
    private lateinit var getVerificationCodeUseCase: GetVerificationCodeUseCase
    private lateinit var checkVerificationCodeUseCase: CheckVerificationCodeUseCase
    private lateinit var changeEmailUseCase: ChangeEmailUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var changePasswordUseCase: ChangePasswordUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserUseCase = mock(GetUserUseCase::class.java)
        getUserPreferencesUseCase = mock(GetUserPreferencesUseCase::class.java)
        updateUserPreferencesUseCase = mock(UpdateUserPreferencesUseCase::class.java)
        getVerificationCodeUseCase = mock(GetVerificationCodeUseCase::class.java)
        checkVerificationCodeUseCase = mock(CheckVerificationCodeUseCase::class.java)
        changeEmailUseCase = mock(ChangeEmailUseCase::class.java)
        logoutUseCase = mock(LogoutUseCase::class.java)
        deleteAccountUseCase = mock(DeleteAccountUseCase::class.java)
        changePasswordUseCase = mock(ChangePasswordUseCase::class.java)
        viewModel = SettingsViewModel(
            getUserUseCase = getUserUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updateUserPreferencesUseCase = updateUserPreferencesUseCase,
            getVerificationCodeUseCase = getVerificationCodeUseCase,
            checkVerificationCodeUseCase = checkVerificationCodeUseCase,
            changeEmailUseCase = changeEmailUseCase,
            logoutUseCase = logoutUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load profile data success opens main state`() = runTest {
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.success(currentUser()))
        `when`(getUserPreferencesUseCase.execute()).thenReturn(Result.success(currentPreferences()))

        viewModel.obtainEvent(SettingsEvent.LoadProfileData)
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is SettingsState.Main)
        state as SettingsState.Main
        assertEquals("user@mail.com", state.email)
        assertTrue(state.matchNotificationsEnabled)
    }

    @Test
    fun `load profile data failure sets network fatal error`() = runTest {
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(SettingsEvent.LoadProfileData)
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is SettingsState.Error)
        assertEquals(SettingsState.SettingsError.NETWORK_FATAL, (state as SettingsState.Error).error)
    }

    @Test
    fun `notification toggles update state`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(
            updateUserPreferencesUseCase.execute(
                matchNotificationsEnabled = false,
                messageNotificationsEnabled = true,
                hideMyPostsFromNonMatches = false,
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(SettingsEvent.MatchNotificationsToggled)
        advanceUntilIdle()

        val state = viewModel.state.first() as SettingsState.Main
        assertTrue(!state.matchNotificationsEnabled)
    }

    @Test
    fun `toggle notifications rollback on failure`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(
            updateUserPreferencesUseCase.execute(
                matchNotificationsEnabled = false,
                messageNotificationsEnabled = true,
                hideMyPostsFromNonMatches = false,
            )
        ).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(SettingsEvent.MatchNotificationsToggled)
        advanceUntilIdle()

        val state = viewModel.state.first() as SettingsState.Main
        assertTrue(state.matchNotificationsEnabled)
    }

    @Test
    fun `message notifications and hide posts toggles update state`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(
            updateUserPreferencesUseCase.execute(
                matchNotificationsEnabled = true,
                messageNotificationsEnabled = false,
                hideMyPostsFromNonMatches = false,
            )
        ).thenReturn(Result.success(Unit))
        `when`(
            updateUserPreferencesUseCase.execute(
                matchNotificationsEnabled = true,
                messageNotificationsEnabled = true,
                hideMyPostsFromNonMatches = true,
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(SettingsEvent.MessageNotificationsToggled)
        viewModel.obtainEvent(SettingsEvent.HideMyPostsFromNonMatchesToggled)
        advanceUntilIdle()

        val state = viewModel.state.first() as SettingsState.Main
        assertTrue(state.messageNotificationsEnabled)
        assertTrue(state.hideMyPostsFromNonMatches)
    }

    @Test
    fun `edit password opens change password state and visibility toggles work`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.EditPasswordButtonClicked)
        viewModel.obtainEvent(SettingsEvent.ShowOldPasswordButtonClicked)
        viewModel.obtainEvent(SettingsEvent.ShowNewPasswordButtonClicked)
        viewModel.obtainEvent(SettingsEvent.ShowNewPasswordRepeatButtonClicked)

        val state = viewModel.state.first() as SettingsState.ChangePassword
        assertEquals(true, state.oldPasswordVisibility)
        assertEquals(true, state.newPasswordVisibility)
        assertEquals(true, state.newPasswordRepeatVisibility)
    }

    @Test
    fun `save password validation errors are shown`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditPasswordButtonClicked)

        viewModel.obtainEvent(
            SettingsEvent.SavePasswordButtonClicked(
                newPassword = "short",
                oldPassword = "old-pass",
                newPasswordRepeat = "short"
            )
        )

        val state = viewModel.state.first() as SettingsState.ChangePassword
        assertEquals(SettingsState.SettingsError.PASSWORD_LENGTH, state.newPasswordError)
    }

    @Test
    fun `save password success returns to main state`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditPasswordButtonClicked)
        `when`(changePasswordUseCase.execute("old-pass", "Password123!")).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(
            SettingsEvent.SavePasswordButtonClicked(
                newPassword = "Password123!",
                oldPassword = "old-pass",
                newPasswordRepeat = "Password123!"
            )
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.first() is SettingsState.Main)
    }

    @Test
    fun `save password invalid credentials sets incorrect old password error`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditPasswordButtonClicked)
        `when`(changePasswordUseCase.execute("old-pass", "Password123!")).thenReturn(
            Result.failure(InvalidCredentialsException())
        )

        viewModel.obtainEvent(
            SettingsEvent.SavePasswordButtonClicked(
                newPassword = "Password123!",
                oldPassword = "old-pass",
                newPasswordRepeat = "Password123!"
            )
        )
        advanceUntilIdle()

        val state = viewModel.state.first() as SettingsState.ChangePassword
        assertEquals(SettingsState.SettingsError.PASSWORD_INCORRECT, state.oldPasswordError)
    }

    @Test
    fun `edit email save and code flow succeeds`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditEmailButtonClicked)
        viewModel.obtainEvent(SettingsEvent.EmailChanged("new@mail.com"))
        `when`(getVerificationCodeUseCase.execute("new@mail.com")).thenReturn(Result.success(Unit))
        `when`(checkVerificationCodeUseCase.execute("123456", "new@mail.com")).thenReturn(Result.success(true))
        `when`(changeEmailUseCase.execute("new@mail.com")).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(SettingsEvent.SaveEmailButtonClicked)
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.CodeSubmitted("123456"))
        advanceUntilIdle()

        val state = viewModel.state.first() as SettingsState.Main
        assertEquals("new@mail.com", state.email)
    }

    @Test
    fun `save email invalid format sets email error`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditEmailButtonClicked)
        viewModel.obtainEvent(SettingsEvent.EmailChanged("bad"))

        viewModel.obtainEvent(SettingsEvent.SaveEmailButtonClicked)

        val state = viewModel.state.first() as SettingsState.ChangeEmail
        assertEquals(SettingsState.SettingsError.EMAIL_CONTENT, state.emailError)
    }

    @Test
    fun `code submitted invalid and network errors are handled`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditEmailButtonClicked)
        viewModel.obtainEvent(SettingsEvent.EmailChanged("new@mail.com"))
        `when`(getVerificationCodeUseCase.execute("new@mail.com")).thenReturn(Result.success(Unit))
        viewModel.obtainEvent(SettingsEvent.SaveEmailButtonClicked)
        advanceUntilIdle()

        `when`(checkVerificationCodeUseCase.execute("123456", "new@mail.com")).thenReturn(Result.success(false))
        viewModel.obtainEvent(SettingsEvent.CodeSubmitted("123456"))
        advanceUntilIdle()
        var state = viewModel.state.first() as SettingsState.ChangeEmail
        assertEquals(SettingsState.SettingsError.INVALID_CODE, state.codeError)

        `when`(checkVerificationCodeUseCase.execute("123456", "new@mail.com")).thenReturn(Result.failure(RuntimeException()))
        viewModel.obtainEvent(SettingsEvent.CodeSubmitted("123456"))
        advanceUntilIdle()
        state = viewModel.state.first() as SettingsState.ChangeEmail
        assertEquals(SettingsState.SettingsError.NETWORK, state.codeError)
    }

    @Test
    fun `delete account success emits intro navigation`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(deleteAccountUseCase.execute()).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(SettingsEvent.DeleteAccountButtonClicked)
        advanceUntilIdle()

        assertEquals(SettingsAction.NavigateToIntro, viewModel.action.first())
    }

    @Test
    fun `delete account failure sets deletion error state`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(deleteAccountUseCase.execute()).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(SettingsEvent.DeleteAccountButtonClicked)
        advanceUntilIdle()

        assertEquals(SettingsState.DeletionError, viewModel.state.first())
    }

    @Test
    fun `logout success emits intro navigation`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(logoutUseCase.execute()).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(SettingsEvent.LogoutButtonClicked)
        advanceUntilIdle()

        assertEquals(SettingsAction.NavigateToIntro, viewModel.action.first())
    }

    @Test
    fun `logout failure sets error state`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(logoutUseCase.execute()).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(SettingsEvent.LogoutButtonClicked)
        advanceUntilIdle()

        val state = viewModel.state.first() as SettingsState.Error
        assertEquals(SettingsState.SettingsError.NETWORK, state.error)
    }

    @Test
    fun `back from change password returns to main`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditPasswordButtonClicked)
        viewModel.obtainEvent(SettingsEvent.Back)

        assertTrue(viewModel.state.first() is SettingsState.Main)
    }

    @Test
    fun `back from change email code step returns to email step`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.EditEmailButtonClicked)
        viewModel.obtainEvent(SettingsEvent.EmailChanged("new@mail.com"))
        `when`(getVerificationCodeUseCase.execute("new@mail.com")).thenReturn(Result.success(Unit))
        viewModel.obtainEvent(SettingsEvent.SaveEmailButtonClicked)
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.Back)

        val state = viewModel.state.first() as SettingsState.ChangeEmail
        assertTrue(!state.isCode)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.Clear)

        assertEquals(SettingsState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun loadMainState() {
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.success(currentUser()))
        `when`(getUserPreferencesUseCase.execute()).thenReturn(Result.success(currentPreferences()))
        viewModel.obtainEvent(SettingsEvent.LoadProfileData)
    }

    private fun currentUser() = User(
        id = "user-id",
        fandoms = emptyList(),
        name = "User",
        gender = Gender.NOT_SPECIFIED,
        age = 20,
        profileType = ProfileType.Own(login = "login", email = "user@mail.com"),
    )

    private fun currentPreferences() = UserPreferences(
        matchesEnabled = true,
        messagesEnabled = false,
        hideMyPostsFromNonMatches = false,
    )
}
