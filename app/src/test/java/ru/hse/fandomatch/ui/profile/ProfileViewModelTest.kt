package ru.hse.fandomatch.ui.profile

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
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.model.OtherProfileItem
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.model.User
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetUserPostsUseCase
import ru.hse.fandomatch.domain.usecase.posts.LikePostUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendRequestsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var getUserUseCase: GetUserUseCase
    private lateinit var likeOrDislikeProfileUseCase: LikeOrDislikeProfileUseCase
    private lateinit var getUserPostsUseCase: GetUserPostsUseCase
    private lateinit var getFriendsUseCase: GetFriendsUseCase
    private lateinit var getFriendRequestsUseCase: GetFriendRequestsUseCase
    private lateinit var likePostUseCase: LikePostUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserUseCase = mock(GetUserUseCase::class.java)
        likeOrDislikeProfileUseCase = mock(LikeOrDislikeProfileUseCase::class.java)
        getUserPostsUseCase = mock(GetUserPostsUseCase::class.java)
        getFriendsUseCase = mock(GetFriendsUseCase::class.java)
        getFriendRequestsUseCase = mock(GetFriendRequestsUseCase::class.java)
        likePostUseCase = mock(LikePostUseCase::class.java)
        viewModel = ProfileViewModel(
            getUserUseCase = getUserUseCase,
            likeOrDislikeProfileUseCase = likeOrDislikeProfileUseCase,
            getUserPostsUseCase = getUserPostsUseCase,
            getFriendsUseCase = getFriendsUseCase,
            getFriendRequestsUseCase = getFriendRequestsUseCase,
            likePostUseCase = likePostUseCase,
            dispatcherIO = testDispatcher,
            dispatcherMain = testDispatcher,
        )
    }

    @Test
    fun `load profile success sets main state`() = runTest {
        `when`(getUserUseCase.execute("1", true)).thenReturn(Result.success(user()))
        `when`(getUserPostsUseCase.execute("1", true)).thenReturn(Result.success(listOf(post("p1"))))

        viewModel.obtainEvent(ProfileEvent.LoadProfile("1", true))
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is ProfileState.Main)
        state as ProfileState.Main
        assertEquals("1", state.id)
        assertEquals("john", state.login)
        assertTrue(state.bottomSheetState is ProfileState.BottomSheetState.Posts)
    }

    @Test
    fun `load profile failure sets error state`() = runTest {
        `when`(getUserUseCase.execute("1", true)).thenReturn(Result.failure(RuntimeException()))

        viewModel.obtainEvent(ProfileEvent.LoadProfile("1", true))
        advanceUntilIdle()

        assertEquals(ProfileState.Error, viewModel.state.first())
    }

    @Test
    fun `message edit settings and add post buttons emit navigation actions`() = runTest {
        assertNavigationAction(ProfileEvent.MessageButtonClicked("10"), ProfileAction.GoToMessages("10"))
        assertNavigationAction(ProfileEvent.EditProfileButtonClicked, ProfileAction.GoToEditProfile)
        assertNavigationAction(ProfileEvent.SettingsButtonClicked, ProfileAction.GoToSettings)
        assertNavigationAction(ProfileEvent.AddPostButtonClicked, ProfileAction.GoToAddPost)
    }

    @Test
    fun `like and dislike profile emit go to matches action`() = runTest {
        loadMainState()
        advanceUntilIdle()

        `when`(likeOrDislikeProfileUseCase.execute("profile-1", true)).thenReturn(Result.success(Unit))
        `when`(likeOrDislikeProfileUseCase.execute("profile-1", false)).thenReturn(Result.success(Unit))

        viewModel.obtainEvent(ProfileEvent.LikeButtonClicked("profile-1"))
        advanceUntilIdle()
        assertEquals(ProfileAction.GoToMatches, viewModel.action.first())

        viewModel.obtainEvent(ProfileEvent.DislikeButtonClicked("profile-1"))
        advanceUntilIdle()
        assertEquals(ProfileAction.GoToMatches, viewModel.action.first())
    }

    @Test
    fun `friends requests and posts buttons load bottom sheets`() = runTest {
        loadMainState()
        advanceUntilIdle()

        `when`(getFriendsUseCase.execute()).thenReturn(Result.success(listOf(otherProfile("friend-1"))))
        `when`(getFriendRequestsUseCase.execute()).thenReturn(Result.success(listOf(otherProfile("req-1"))))
        `when`(getUserPostsUseCase.execute("1", true)).thenReturn(Result.success(listOf(post("p1"))))

        viewModel.obtainEvent(ProfileEvent.FriendsButtonClicked)
        advanceUntilIdle()
        var state = viewModel.state.first() as ProfileState.Main
        assertTrue(state.bottomSheetState is ProfileState.BottomSheetState.Friends)

        viewModel.obtainEvent(ProfileEvent.RequestsButtonClicked)
        advanceUntilIdle()
        state = viewModel.state.first() as ProfileState.Main
        assertTrue(state.bottomSheetState is ProfileState.BottomSheetState.Requests)

        viewModel.obtainEvent(ProfileEvent.PostsButtonClicked)
        advanceUntilIdle()
        state = viewModel.state.first() as ProfileState.Main
        assertTrue(state.bottomSheetState is ProfileState.BottomSheetState.Posts)
    }

    @Test
    fun `profile and post clicked emit navigation actions`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(ProfileEvent.ProfileClicked("2"))
        assertEquals(ProfileAction.GoToProfile("2"), viewModel.action.first())

        viewModel.obtainEvent(ProfileEvent.PostClicked("post-2"))
        assertEquals(ProfileAction.GoToPost("post-2"), viewModel.action.first())
    }

    @Test
    fun `post liked toggles like count in posts bottom sheet`() = runTest {
        loadMainState()
        advanceUntilIdle()

        `when`(likePostUseCase.execute("post-1")).thenReturn(Result.success(Unit))
        `when`(getUserPostsUseCase.execute("1", true)).thenReturn(Result.success(listOf(post("post-1", liked = false, likeCount = 3))))

        viewModel.obtainEvent(ProfileEvent.PostsButtonClicked)
        advanceUntilIdle()
        viewModel.obtainEvent(ProfileEvent.PostLiked("post-1"))
        advanceUntilIdle()

        val state = viewModel.state.first() as ProfileState.Main
        val posts = (state.bottomSheetState as ProfileState.BottomSheetState.Posts).posts
        assertEquals(4, posts.first().likeCount)
        assertTrue(posts.first().isLikedByCurrentUser)
    }

    @Test
    fun `clear resets state and action`() = runTest {
        loadMainState()
        advanceUntilIdle()

        viewModel.obtainEvent(ProfileEvent.Clear)

        assertEquals(ProfileState.Idle, viewModel.state.first())
        assertEquals(null, viewModel.action.first())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun loadMainState() {
        `when`(getUserUseCase.execute("1", true)).thenReturn(Result.success(user()))
        `when`(getUserPostsUseCase.execute("1", true)).thenReturn(Result.success(listOf(post("p1"))))
        viewModel.obtainEvent(ProfileEvent.LoadProfile("1", true))
    }

    private suspend fun assertNavigationAction(event: ProfileEvent, expectedAction: ProfileAction) {
        loadMainState()
        viewModel.obtainEvent(event)
        assertEquals(expectedAction, viewModel.action.first())
    }

    private fun user() = User(
        id = "1",
        fandoms = listOf(Fandom("1", "Fandom1", FandomCategory.BOOKS)),
        description = "bio",
        name = "John Doe",
        gender = Gender.MALE,
        age = 20,
        avatar = MediaItem("avatar-1", MediaType.IMAGE, "avatar-url"),
        background = MediaItem("background-1", MediaType.IMAGE, "background-url"),
        city = City("Москва", "Moscow"),
        profileType = ProfileType.Own(login = "john", email = "john@mail.com"),
    )

    private fun post(id: String, liked: Boolean = false, likeCount: Int = 2) = Post(
        id = id,
        authorId = "author-1",
        authorName = "Author",
        authorLogin = "author_login",
        authorAvatar = MediaItem("avatar", MediaType.IMAGE, "url"),
        timestamp = 1000L,
        content = "content",
        mediaItems = emptyList(),
        likeCount = likeCount,
        commentCount = 0,
        isLikedByCurrentUser = liked,
        fandoms = emptyList(),
    )

    private fun otherProfile(id: String) = OtherProfileItem(
        id = id,
        name = "Name$id",
        login = "login$id",
        avatar = null,
    )
}
