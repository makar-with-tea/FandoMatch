package ru.hse.fandomatch.ui.editprofile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import ru.hse.fandomatch.domain.model.City
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.fandoms.GetFandomsByQueryUseCase
import ru.hse.fandomatch.domain.usecase.user.EditProfileUseCase
import ru.hse.fandomatch.domain.usecase.user.GetCitiesByQueryUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EditProfileViewModel
    private lateinit var getFandomsByQueryUseCase: GetFandomsByQueryUseCase
    private lateinit var getCitiesByQueryUseCase: GetCitiesByQueryUseCase
    private lateinit var getUserUseCase: GetUserUseCase
    private lateinit var uploadMediaUseCase: UploadMediaUseCase
    private lateinit var editProfileUseCase: EditProfileUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getFandomsByQueryUseCase = mock(GetFandomsByQueryUseCase::class.java)
        getCitiesByQueryUseCase = mock(GetCitiesByQueryUseCase::class.java)
        getUserUseCase = mock(GetUserUseCase::class.java)
        uploadMediaUseCase = mock(UploadMediaUseCase::class.java)
        editProfileUseCase = mock(EditProfileUseCase::class.java)
        viewModel = EditProfileViewModel(
            getFandomsByQueryUseCase = getFandomsByQueryUseCase,
            getCitiesByQueryUseCase = getCitiesByQueryUseCase,
            getUserUseCase = getUserUseCase,
            uploadMediaUseCase = uploadMediaUseCase,
            editProfileUseCase = editProfileUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load profile data success moves to main state`() = runTest {
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.success(user()))

        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is EditProfileState.Main)
        state as EditProfileState.Main
        assertEquals("John", state.name)
        assertEquals("john", state.login)
        assertEquals(1, state.fandoms.size)
    }

    @Test
    fun `load profile data failure sets error state`() = runTest {
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
        advanceUntilIdle()

        assertEquals(EditProfileState.Error, viewModel.state.value)
    }

    @Test
    fun `name changed validates name`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(EditProfileEvent.NameChanged("A"))
        var state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.NAME_LENGTH, state.nameError)

        viewModel.obtainEvent(EditProfileEvent.NameChanged("Ivan Ivanov"))
        state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.IDLE, state.nameError)
    }

    @Test
    fun `description changed validates length`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(EditProfileEvent.DescriptionChanged("a".repeat(2000)))
        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(EditProfileState.EditProfileError.DESCRIPTION_LENGTH, state.descriptionError)
    }

    @Test
    fun `avatar and background changed update bytes`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(EditProfileEvent.AvatarChanged(byteArrayOf(1, 2, 3)))
        viewModel.obtainEvent(EditProfileEvent.BackgroundChanged(byteArrayOf(4, 5, 6)))

        val state = viewModel.state.value as EditProfileState.Main
        assertEquals(true, state.avatarBytes?.isNotEmpty())
        assertEquals(true, state.backgroundBytes?.isNotEmpty())
    }

    @Test
    fun `fandom added removed and searched update state`() = runTest {
        loadMainState()
        advanceUntilIdle()
        val fandom = fandom("2")
        `when`(getFandomsByQueryUseCase.execute("Star")).thenReturn(Result.success(listOf(fandom)))

        viewModel.obtainEvent(EditProfileEvent.FandomAdded(fandom))
        var state = viewModel.state.value as EditProfileState.Main
        assertTrue(fandom in state.fandoms)

        viewModel.obtainEvent(EditProfileEvent.FandomRemoved(fandom))
        state = viewModel.state.value as EditProfileState.Main
        assertTrue(fandom !in state.fandoms)

        viewModel.obtainEvent(EditProfileEvent.FandomSearched("Star"))
        advanceUntilIdle()
        state = viewModel.state.value as EditProfileState.Main
        assertEquals(listOf(fandom), state.foundFandoms)
        assertFalse(state.areFandomsLoading)

        viewModel.obtainEvent(EditProfileEvent.FandomSearched(""))
        state = viewModel.state.value as EditProfileState.Main
        assertTrue(state.foundFandoms.isEmpty())
    }

    @Test
    fun `city searched and selected update state`() = runTest {
        loadMainState()
        advanceUntilIdle()
        val cities = listOf(City("Москва", "Moscow"))
        `when`(getCitiesByQueryUseCase.execute("Mos")).thenReturn(Result.success(cities))

        viewModel.obtainEvent(EditProfileEvent.CitySearched("Mos"))
        advanceUntilIdle()
        var state = viewModel.state.value as EditProfileState.Main
        assertEquals(cities, state.foundCities)
        assertFalse(state.areCitiesLoading)

        viewModel.obtainEvent(EditProfileEvent.CitySelected(cities.first()))
        state = viewModel.state.value as EditProfileState.Main
        assertEquals(cities.first(), state.city)
    }

    @Test
    fun `add fandom button emits navigation action`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(EditProfileEvent.AddFandomButtonClicked)

        assertEquals(EditProfileAction.NavigateToAddFandom, viewModel.action.value)
    }

    @Test
    fun `save button success emits navigate action`() = runTest {
        loadMainState()
        advanceUntilIdle()
        val avatarBytes = byteArrayOf(1, 2, 3)
        val backgroundBytes = byteArrayOf(4, 5, 6)
        viewModel.obtainEvent(EditProfileEvent.NameChanged("John Doe"))
        viewModel.obtainEvent(EditProfileEvent.AvatarChanged(avatarBytes))
        viewModel.obtainEvent(EditProfileEvent.BackgroundChanged(backgroundBytes))
        `when`(uploadMediaUseCase.execute(avatarBytes, MediaType.IMAGE)).thenReturn(Result.success("avatar-id"))
        `when`(uploadMediaUseCase.execute(backgroundBytes, MediaType.IMAGE)).thenReturn(Result.success("background-id"))
        `when`(
            editProfileUseCase.execute(
                name = "John Doe",
                bio = "bio",
                city = City("Москва", "Moscow"),
                fandoms = listOf(fandom("1")),
                avatarMediaId = "avatar-id",
                backgroundMediaId = "background-id",
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(EditProfileEvent.SaveButtonClicked)
        advanceUntilIdle()

        assertEquals(EditProfileAction.NavigateToMyProfile, viewModel.action.value)
    }

    @Test
    fun `save button upload failure emits error toast`() = runTest {
        loadMainState()
        advanceUntilIdle()
        val avatarBytes = byteArrayOf(1, 2, 3)
        viewModel.obtainEvent(EditProfileEvent.AvatarChanged(avatarBytes))
        `when`(uploadMediaUseCase.execute(avatarBytes, MediaType.IMAGE)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(EditProfileEvent.SaveButtonClicked)
        advanceUntilIdle()

        assertEquals(EditProfileAction.ShowErrorToast, viewModel.action.value)
    }

    @Test
    fun `save button profile update failure emits error toast and toast shown clears it`() = runTest {
        loadMainState()
        advanceUntilIdle()
        `when`(
            editProfileUseCase.execute(
                name = "John",
                bio = "bio",
                city = City("Москва", "Moscow"),
                fandoms = listOf(fandom("1")),
                avatarMediaId = "avatar-1",
                backgroundMediaId = "background-1",
            )
        ).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(EditProfileEvent.SaveButtonClicked)
        advanceUntilIdle()
        assertEquals(EditProfileAction.ShowErrorToast, viewModel.action.value)

        viewModel.obtainEvent(EditProfileEvent.ToastShown)
        assertEquals(null, viewModel.action.value)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        loadMainState()
        advanceUntilIdle()
        viewModel.obtainEvent(EditProfileEvent.Clear)

        assertEquals(EditProfileState.Idle, viewModel.state.value)
        assertEquals(null, viewModel.action.value)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun loadMainState() {
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.success(user()))
        viewModel.obtainEvent(EditProfileEvent.LoadProfileData)
    }

    private fun user() = User(
        id = "1",
        fandoms = listOf(fandom("1")),
        description = "bio",
        name = "John",
        gender = ru.hse.fandomatch.domain.model.Gender.MALE,
        age = 20,
        avatar = MediaItem("avatar-1", MediaType.IMAGE, "avatar-url"),
        background = MediaItem("background-1", MediaType.IMAGE, "background-url"),
        city = City("Москва", "Moscow"),
        profileType = ProfileType.Own(login = "john", email = "john@mail.com"),
    )

    private fun fandom(id: String) = Fandom(
        id = id,
        name = "Fandom$id",
        category = FandomCategory.BOOKS,
    )
}
