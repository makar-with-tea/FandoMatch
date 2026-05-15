package ru.hse.fandomatch.ui.registration

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
import ru.hse.fandomatch.domain.exception.LoginAlreadyInUseException
import ru.hse.fandomatch.domain.logging.Logger
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.usecase.auth.CheckVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.RegisterUseCase
import ru.hse.fandomatch.domain.usecase.media.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.user.EditProfileUseCase
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RegistrationViewModel
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var getVerificationCodeUseCase: GetVerificationCodeUseCase
    private lateinit var checkVerificationCodeUseCase: CheckVerificationCodeUseCase
    private lateinit var uploadMediaUseCase: UploadMediaUseCase
    private lateinit var editProfileUseCase: EditProfileUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registerUseCase = mock(RegisterUseCase::class.java)
        getVerificationCodeUseCase = mock(GetVerificationCodeUseCase::class.java)
        checkVerificationCodeUseCase = mock(CheckVerificationCodeUseCase::class.java)
        uploadMediaUseCase = mock(UploadMediaUseCase::class.java)
        editProfileUseCase = mock(EditProfileUseCase::class.java)
        viewModel = RegistrationViewModel(
            registerUseCase = registerUseCase,
            getVerificationCodeUseCase = getVerificationCodeUseCase,
            checkVerificationCodeUseCase = checkVerificationCodeUseCase,
            uploadMediaUseCase = uploadMediaUseCase,
            editProfileUseCase = editProfileUseCase,
            logger = Logger.NoOpLogger,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `name email and login changes validate input`() = runTest {
        viewModel.obtainEvent(RegistrationEvent.NameChanged("A"))
        viewModel.obtainEvent(RegistrationEvent.EmailChanged("bad"))
        viewModel.obtainEvent(RegistrationEvent.LoginChanged("ab"))

        val state = viewModel.state.first() as RegistrationState.Name
        assertEquals(RegistrationState.RegistrationError.NAME_LENGTH, state.nameError)
        assertEquals(RegistrationState.RegistrationError.EMAIL_CONTENT, state.emailError)
        assertEquals(RegistrationState.RegistrationError.LOGIN_LENGTH, state.loginError)
    }

    @Test
    fun `name submitted success moves to code step`() = runTest {
        fillNameStep()
        `when`(getVerificationCodeUseCase.execute(profileType.email)).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(RegistrationEvent.NameSubmitted)
        advanceUntilIdle()

        assertTrue(viewModel.state.first() is RegistrationState.Code)
    }

    @Test
    fun `name submitted failure sets network errors`() = runTest {
        fillNameStep()
        `when`(getVerificationCodeUseCase.execute(profileType.email)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(RegistrationEvent.NameSubmitted)
        advanceUntilIdle()

        val state = viewModel.state.first() as RegistrationState.Name
        assertEquals(RegistrationState.RegistrationError.NETWORK, state.nameError)
        assertEquals(RegistrationState.RegistrationError.NETWORK, state.emailError)
        assertEquals(RegistrationState.RegistrationError.NETWORK, state.loginError)
    }

    @Test
    fun `code submitted invalid shows invalid code error`() = runTest {
        goToCodeStep()
        advanceUntilIdle()
        `when`(checkVerificationCodeUseCase.execute(code, profileType.email)).thenReturn(
            Result.success(false)
        )

        viewModel.obtainEvent(RegistrationEvent.CodeSubmitted(code))
        advanceUntilIdle()

        val state = viewModel.state.first() as RegistrationState.Code
        assertEquals(RegistrationState.RegistrationError.INVALID_CODE, state.codeError)
    }

    @Test
    fun `code submitted network failure shows network error`() = runTest {
        goToCodeStep()
        advanceUntilIdle()
        `when`(checkVerificationCodeUseCase.execute(code, profileType.email)).thenReturn(
            Result.failure(RuntimeException())
        )

        viewModel.obtainEvent(RegistrationEvent.CodeSubmitted(code))
        advanceUntilIdle()

        val state = viewModel.state.first() as RegistrationState.Code
        assertEquals(RegistrationState.RegistrationError.NETWORK, state.codeError)
    }

    @Test
    fun `date selected validates minimum age`() = runTest {
        goToDateOfBirthStep()
        advanceUntilIdle()

        viewModel.obtainEvent(RegistrationEvent.DateSelected(dateEpochSeconds(LocalDate.now().minusYears(10))))

        val state = viewModel.state.first() as RegistrationState.DateOfBirth
        assertEquals(RegistrationState.RegistrationError.DOB_TOO_YOUNG, state.error)
    }

    @Test
    fun `date selected success moves to gender choice`() = runTest {
        goToDateOfBirthStep()
        advanceUntilIdle()

        viewModel.obtainEvent(RegistrationEvent.DateSelected(dateEpochSeconds(LocalDate.now().minusYears(20))))

        val state = viewModel.state.first()
        assertTrue(state is RegistrationState.GenderChoice)
    }

    @Test
    fun `gender selected moves to avatar step`() = runTest {
        goToGenderChoiceStep()
        advanceUntilIdle()

        viewModel.obtainEvent(RegistrationEvent.GenderSelected(Gender.FEMALE))

        val state = viewModel.state.first() as RegistrationState.Avatar
        assertTrue(state.avatarByteArray == null)
    }

    @Test
    fun `avatar selected moves to password step`() = runTest {
        goToAvatarStep()
        advanceUntilIdle()

        viewModel.obtainEvent(RegistrationEvent.AvatarSelected(byteArrayOf(1, 2, 3)))

        val state = viewModel.state.first() as RegistrationState.Password
        assertEquals("", state.password)
        assertEquals("", state.passwordRepeat)
    }

    @Test
    fun `password visibility events toggle flags`() = runTest {
        goToPasswordStep()
        advanceUntilIdle()

        viewModel.obtainEvent(RegistrationEvent.PasswordVisibilityChanged)
        viewModel.obtainEvent(RegistrationEvent.PasswordRepeatVisibilityChanged)

        val state = viewModel.state.first() as RegistrationState.Password
        assertTrue(state.passwordVisibility)
        assertTrue(state.passwordRepeatVisibility)
    }

    @Test
    fun `password submit with invalid password shows validation errors`() = runTest {
        goToPasswordStep()
        advanceUntilIdle()
        viewModel.obtainEvent(RegistrationEvent.PasswordChanged("short"))
        viewModel.obtainEvent(RegistrationEvent.PasswordRepeatChanged("short"))
        viewModel.obtainEvent(RegistrationEvent.AgreedToTermsChanged(true))

        viewModel.obtainEvent(RegistrationEvent.PasswordSubmit)

        val state = viewModel.state.first() as RegistrationState.Password
        assertEquals(RegistrationState.RegistrationError.PASSWORD_LENGTH, state.passwordError)
    }

    @Test
    fun `password submit success emits navigate to matches action`() = runTest {
        goToPasswordStep(byteArrayOf(7, 8, 9), Gender.MALE)
        advanceUntilIdle()
        viewModel.obtainEvent(RegistrationEvent.PasswordChanged(password))
        viewModel.obtainEvent(RegistrationEvent.PasswordRepeatChanged(password))
        viewModel.obtainEvent(RegistrationEvent.AgreedToTermsChanged(true))
        `when`(uploadMediaUseCase.execute(byteArrayOf(7, 8, 9), MediaType.IMAGE)).thenReturn(
            Result.success("avatar-id")
        )
        `when`(
            registerUseCase.execute(
                name = user.name,
                email = profileType.email,
                login = profileType.login,
                dateOfBirthEpochSeconds = dateEpochSeconds(LocalDate.now().minusYears(user.age.toLong())),
                gender = user.gender,
                password = password
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(RegistrationEvent.PasswordSubmit)
        advanceUntilIdle()

        assertEquals(RegistrationAction.NavigateToMatches, viewModel.action.first())
    }

    @Test
    fun `password submit with taken login returns to name step with login taken error`() = runTest {
        goToPasswordStep(null, Gender.MALE)
        advanceUntilIdle()
        viewModel.obtainEvent(RegistrationEvent.PasswordChanged(password))
        viewModel.obtainEvent(RegistrationEvent.PasswordRepeatChanged(password))
        viewModel.obtainEvent(RegistrationEvent.AgreedToTermsChanged(true))
        `when`(
            registerUseCase.execute(
                name = user.name,
                email = profileType.email,
                login = profileType.login,
                dateOfBirthEpochSeconds = dateEpochSeconds(LocalDate.now().minusYears(user.age.toLong())),
                gender = user.gender,
                password = password
            )
        ).thenReturn(Result.failure(LoginAlreadyInUseException()))

        viewModel.obtainEvent(RegistrationEvent.PasswordSubmit)
        advanceUntilIdle()

        val state = viewModel.state.first() as RegistrationState.Name
        assertEquals(RegistrationState.RegistrationError.LOGIN_TAKEN, state.loginError)
    }

    @Test
    fun `back from password returns to avatar`() = runTest {
        goToPasswordStep()
        advanceUntilIdle()
        viewModel.obtainEvent(RegistrationEvent.Back)

        val state = viewModel.state.first() as RegistrationState.Avatar
        assertEquals(null, state.avatarByteArray)
    }

    @Test
    fun `back from initial name step emits navigate back action`() = runTest {
        viewModel.obtainEvent(RegistrationEvent.Back)

        assertEquals(RegistrationAction.NavigateBack, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        goToPasswordStep()
        advanceUntilIdle()
        viewModel.obtainEvent(RegistrationEvent.Clear)

        assertEquals(RegistrationState.Name(), viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillNameStep(
        name: String = user.name,
        email: String = profileType.email,
        login: String = profileType.login,
    ) {
        viewModel.obtainEvent(RegistrationEvent.NameChanged(name))
        viewModel.obtainEvent(RegistrationEvent.EmailChanged(email))
        viewModel.obtainEvent(RegistrationEvent.LoginChanged(login))
    }

    private suspend fun goToCodeStep() {
        fillNameStep(user.name, profileType.email, profileType.login)
        `when`(getVerificationCodeUseCase.execute(profileType.email)).thenReturn(Result.success(Unit))
        viewModel.obtainEvent(RegistrationEvent.NameSubmitted)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private suspend fun goToDateOfBirthStep() {
        goToCodeStep()
        `when`(checkVerificationCodeUseCase.execute(code, profileType.email)).thenReturn(
            Result.success(true)
        )
        viewModel.obtainEvent(RegistrationEvent.CodeSubmitted(code))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private suspend fun goToGenderChoiceStep() {
        goToDateOfBirthStep()
        viewModel.obtainEvent(RegistrationEvent.DateSelected(dateEpochSeconds(LocalDate.now().minusYears(20))))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private suspend fun goToAvatarStep(gender: Gender = Gender.NOT_SPECIFIED) {
        goToGenderChoiceStep()
        viewModel.obtainEvent(RegistrationEvent.GenderSelected(gender))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private suspend fun goToPasswordStep(avatar: ByteArray? = null, gender: Gender = Gender.NOT_SPECIFIED) {
        goToAvatarStep(gender)
        viewModel.obtainEvent(RegistrationEvent.AvatarSelected(avatar))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun dateEpochSeconds(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000

    private companion object {
        val profileType = ProfileType.Own(
            email = "john@mail.com",
            login = "john_doe",
        )
        val code = "123456"
        val password = "qwerty123!"
        val user = User(
            id = "user-1",
            fandoms = listOf(),
            description = null,
            name = "John Doe",
            gender = Gender.MALE,
            age = 20,
            avatar = MediaItem(
                id = "avatar-1",
                mediaType = MediaType.IMAGE,
                url = "http://example.com/avatar.jpg"
            ),
            background = null,
            city = null,
            profileType = profileType
        )
    }
}
