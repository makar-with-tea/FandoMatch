package ru.hse.fandomatch.ui.filters

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
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Filters
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.usecase.fandoms.GetFandomsByQueryUseCase
import ru.hse.fandomatch.domain.usecase.matches.ApplyFiltersUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadInitialFiltersUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class FiltersViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FiltersViewModel
    private lateinit var loadInitialFiltersUseCase: LoadInitialFiltersUseCase
    private lateinit var applyFiltersUseCase: ApplyFiltersUseCase
    private lateinit var getUserUseCase: GetUserUseCase
    private lateinit var getFandomsByQueryUseCase: GetFandomsByQueryUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loadInitialFiltersUseCase = mock(LoadInitialFiltersUseCase::class.java)
        applyFiltersUseCase = mock(ApplyFiltersUseCase::class.java)
        getUserUseCase = mock(GetUserUseCase::class.java)
        getFandomsByQueryUseCase = mock(GetFandomsByQueryUseCase::class.java)
        viewModel = FiltersViewModel(
            loadInitialFiltersUseCase = loadInitialFiltersUseCase,
            applyFiltersUseCase = applyFiltersUseCase,
            getUserUseCase = getUserUseCase,
            getFandomsByQueryUseCase = getFandomsByQueryUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load initial filters success sets main state`() = runTest {
        `when`(loadInitialFiltersUseCase.execute()).thenReturn(
            Result.success(
                Filters(
                    genders = listOf(Gender.FEMALE),
                    minAge = 18,
                    maxAge = 30,
                    categories = listOf(FandomCategory.BOOKS),
                    fandoms = listOf(fandom("f1")),
                    onlyInUserCity = true,
                )
            )
        )
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.success(user()))

        viewModel.obtainEvent(FiltersEvent.LoadInitialFilters)
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is FiltersState.Main)
        state as FiltersState.Main
        assertEquals(18..30, state.ageRange)
        assertEquals(true, state.onlyInUserCity)
    }

    @Test
    fun `load initial filters failure sets error state`() = runTest {
        `when`(loadInitialFiltersUseCase.execute()).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(FiltersEvent.LoadInitialFilters)
        advanceUntilIdle()

        assertEquals(FiltersState.Error, viewModel.state.first())
    }

    @Test
    fun `gender selected toggles gender in main state`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(FiltersEvent.GenderSelected(Gender.FEMALE))

        val state = viewModel.state.first() as FiltersState.Main
        assertEquals(listOf(Gender.MALE), state.selectedGenders)
    }

    @Test
    fun `age range changed updates range`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(FiltersEvent.AgeRangeChanged(20..28))

        val state = viewModel.state.first() as FiltersState.Main
        assertEquals(20..28, state.ageRange)
    }

    @Test
    fun `category toggled adds and removes category`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(FiltersEvent.CategoryToggled(FandomCategory.FILMS))
        var state = viewModel.state.first() as FiltersState.Main
        assertTrue(FandomCategory.FILMS in state.selectedCategories)

        viewModel.obtainEvent(FiltersEvent.CategoryToggled(FandomCategory.FILMS))
        state = viewModel.state.first() as FiltersState.Main
        assertTrue(FandomCategory.FILMS !in state.selectedCategories)
    }

    @Test
    fun `fandom added and removed updates selected list`() = runTest {
        prepareMainState()
        advanceUntilIdle()
        val fandom = fandom("f2")

        viewModel.obtainEvent(FiltersEvent.FandomAdded(fandom))
        var state = viewModel.state.first() as FiltersState.Main
        assertTrue(fandom in state.selectedFandoms)

        viewModel.obtainEvent(FiltersEvent.FandomRemoved(fandom))
        state = viewModel.state.first() as FiltersState.Main
        assertTrue(fandom !in state.selectedFandoms)
    }

    @Test
    fun `fandom searched updates found fandoms`() = runTest {
        prepareMainState()
        advanceUntilIdle()
        val found = listOf(fandom("f3"))
        `when`(getFandomsByQueryUseCase.execute("harry")).thenReturn(Result.success(found))

        viewModel.obtainEvent(FiltersEvent.FandomSearched("harry"))
        advanceUntilIdle()

        val state = viewModel.state.first() as FiltersState.Main
        assertEquals(found, state.foundFandoms)
    }

    @Test
    fun `location toggled updates value`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(FiltersEvent.LocationToggled(true))

        val state = viewModel.state.first() as FiltersState.Main
        assertEquals(true, state.onlyInUserCity)
    }

    @Test
    fun `reset filters applies default values`() = runTest {
        prepareMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(FiltersEvent.ResetFilters)

        val state = viewModel.state.first() as FiltersState.Main
        assertEquals(16..40, state.ageRange)
        assertEquals(emptyList<Fandom>(), state.selectedFandoms)
    }

    @Test
    fun `apply filters success emits navigate to matches action`() = runTest {
        prepareMainState()
        advanceUntilIdle()
        `when`(
            applyFiltersUseCase.execute(
                genders = listOf(Gender.MALE, Gender.FEMALE),
                minAge = 18,
                maxAge = 30,
                categories = listOf(FandomCategory.BOOKS),
                fandoms = listOf(fandom("f1")),
                onlyInUserCity = false,
            )
        ).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(FiltersEvent.ApplyFilters)
        advanceUntilIdle()

        assertEquals(FiltersAction.NavigateToMatches, viewModel.action.first())
    }

    @Test
    fun `apply filters failure emits error toast action and toast shown clears it`() = runTest {
        prepareMainState()
        advanceUntilIdle()
        `when`(
            applyFiltersUseCase.execute(
                genders = listOf(Gender.MALE, Gender.FEMALE),
                minAge = 18,
                maxAge = 30,
                categories = listOf(FandomCategory.BOOKS),
                fandoms = listOf(fandom("f1")),
                onlyInUserCity = false,
            )
        ).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(FiltersEvent.ApplyFilters)
        advanceUntilIdle()
        assertEquals(FiltersAction.ShowErrorToast, viewModel.action.first())

        viewModel.obtainEvent(FiltersEvent.ToastShown)
        assertEquals(null, viewModel.action.first())
    }

    @Test
    fun `add fandom clicked emits navigate to add fandom action`() = runTest {
        viewModel.obtainEvent(FiltersEvent.AddFandomClicked)

        assertEquals(FiltersAction.NavigateToAddFandom, viewModel.action.first())
    }

    @Test
    fun `clear resets state and action`() = runTest {
        viewModel.obtainEvent(FiltersEvent.AddFandomClicked)
        viewModel.obtainEvent(FiltersEvent.Clear)

        assertEquals(FiltersState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun prepareMainState() {
        `when`(loadInitialFiltersUseCase.execute()).thenReturn(
            Result.success(
                Filters(
                    genders = listOf(Gender.MALE, Gender.FEMALE),
                    minAge = 18,
                    maxAge = 30,
                    categories = listOf(FandomCategory.BOOKS),
                    fandoms = listOf(fandom("f1")),
                    onlyInUserCity = false,
                )
            )
        )
        `when`(getUserUseCase.execute(null, true)).thenReturn(Result.success(user()))
        viewModel.obtainEvent(FiltersEvent.LoadInitialFilters)
    }

    private fun fandom(id: String) = Fandom(id, "Fandom$id", FandomCategory.BOOKS)

    private fun user() = User(
        id = "user-id",
        fandoms = emptyList(),
        name = "User",
        gender = Gender.NOT_SPECIFIED,
        age = 20,
        city = City("Москва", "Moscow"),
        profileType = ProfileType.Own(login = "login", email = "user@mail.com"),
    )
}


