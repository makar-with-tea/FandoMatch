package ru.hse.fandomatch.navigation

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.addfandom.AddFandomScreen
import ru.hse.fandomatch.ui.authorization.AuthorizationScreen
import ru.hse.fandomatch.ui.chat.ChatScreen
import ru.hse.fandomatch.ui.chatslist.ChatsListScreen
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.editprofile.EditProfileScreen
import ru.hse.fandomatch.ui.feed.FeedScreen
import ru.hse.fandomatch.ui.filters.FiltersScreen
import ru.hse.fandomatch.ui.intro.IntroScreen
import ru.hse.fandomatch.ui.matches.MatchesScreen
import ru.hse.fandomatch.ui.profile.ProfileScreen
import ru.hse.fandomatch.ui.registration.RegistrationScreen
import ru.hse.fandomatch.ui.settings.SettingsScreen
import ru.hse.fandomatch.orFalse
import ru.hse.fandomatch.ui.newpost.NewPostScreen
import ru.hse.fandomatch.ui.passwordrecovery.PasswordRecoveryScreen
import ru.hse.fandomatch.ui.post.PostScreen

sealed class Route(val route: String) {
    data object Authorization : Route("authorization")
    data object Chat : Route("chat/{chat_id}") {
        fun createRoute(chatId: String?): String {
            return "chat/$chatId"
        }
    }

    data object ChatsList : Route("chats_list")
    data object Feed : Route("feed")
    data object Filters : Route("filters")
    data object Intro : Route("intro")
    data object Matches : Route("matches")
    data object MyProfile : Route("my_profile")
    data object Profile : Route("profile/{profile_id}") {
        fun createRoute(profileId: String): String {
            return "profile/$profileId"
        }
    }

    data object Registration : Route("registration")
    data object EditProfile : Route("edit_profile")
    data object Settings : Route("settings")
    data object AddFandom : Route("add_fandom")
    data object NewPost : Route("new_post")
    data object PasswordRecovery : Route("password_recovery")
    data object Post : Route("post/{post_id}") {
        fun createRoute(postId: String): String {
            return "post/$postId"
        }
    }
}

@Composable
fun MainView() {
    Log.d("MainView", "SetUpNavHost")
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun navigateToRoute(route: Route) {
        navController.navigate(route.route) {
            launchSingleTop = true
            restoreState = true
        }
        Log.d("Navigation", "MainView to ${route.route} from $currentRoute")
    }

    fun navigateToRouteWithArgs(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
        Log.d("Navigation", "MainView to $route from $currentRoute")
    }

    // todo go back from the first screen??

    val screenTitleId =
        when (currentRoute) {
            Route.Authorization.route -> R.string.authorization_title
            Route.Matches.route -> R.string.matches_title
            Route.Filters.route -> R.string.filters_title
            Route.Feed.route -> R.string.feed_title
            Route.AddFandom.route -> R.string.add_fandom_title
            Route.PasswordRecovery.route -> R.string.password_recovery_title
            else -> null
        }

    val endIcons =
        when (currentRoute) {
            Route.Matches.route -> listOf(
                EndIconState(
                    iconId = R.drawable.ic_filters,
                    onClick = { navigateToRoute(Route.Filters) },
                    descriptionId = R.string.filters_icon_description
                )
            )

            else -> listOf()
        }

    val topBarState = remember(
        screenTitleId,
        endIcons
    ) {
        mutableStateOf(screenTitleId?.let {
            TopBarState(
                titleContent = { MyTitle(text = stringResource(screenTitleId)) },
                endIcons = endIcons,
            )
        }
        )
    }

    fun updateTopBar() {
        val screenTitleId =
            when (currentRoute) {
                Route.Authorization.route -> R.string.authorization_title
                Route.Matches.route -> R.string.matches_title
                Route.Filters.route -> R.string.filters_title
                Route.Feed.route -> R.string.feed_title
                Route.AddFandom.route -> R.string.add_fandom_title
                Route.AddFandom.route -> R.string.add_fandom_title
                Route.PasswordRecovery.route -> R.string.password_recovery_title
                Route.Intro.route, Route.Authorization.route -> null
                else -> null
            }

        val endIcons =
            when (currentRoute) {
                Route.Matches.route -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_filters,
                        onClick = { navigateToRoute(Route.Filters) },
                        descriptionId = R.string.filters_icon_description
                    )
                )

                else -> listOf()
            }

        topBarState.value = screenTitleId?.let {
            TopBarState(
                titleContent = { MyTitle(text = stringResource(screenTitleId)) },
                endIcons = endIcons,
            )
        }
    }

    val setTopBarState = { state: TopBarState?, route: String? ->
        if (currentRoute == route) topBarState.value = state
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Scaffold(
        topBar = {
            topBarState.value?.let {
                TopBar(
                    state = it,
                    onBackClick = { backDispatcher?.onBackPressed() }
                )
            }
        },
        bottomBar = {
            Log.d("SetUpNavHost", "Current route: $currentRoute")
            if (currentRoute?.canShowBottomBar().orFalse()) {
                BottomBar(
                    navigateToMatches = { navigateToRoute(Route.Matches) },
                    navigateToMyProfile = { navigateToRoute(Route.MyProfile) },
                    navigateToChats = { navigateToRoute(Route.ChatsList) },
                    navigateToFeed = { navigateToRoute(Route.Feed) },
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
        ) {
            NavHost(navController = navController, startDestination = Route.Intro.route) {
                composable(Route.Intro.route) {
                    updateTopBar()
                    IntroScreen(
                        navigateToMatches = { navigateToRoute(Route.Matches) },
                        navigateToLogin = { navigateToRoute(Route.Authorization) },
                        navigateToRegistration = { navigateToRoute(Route.Registration) },
                    )
                }
                composable(Route.Authorization.route) {
                    updateTopBar()
                    AuthorizationScreen(
                        navigateToMatches = { navigateToRoute(Route.Matches) },
                        navigateToPasswordRecovery = { navigateToRoute(Route.PasswordRecovery) },
                    )
                }
                composable(Route.Registration.route) {
                    RegistrationScreen(
                        navigateToMatches = { navigateToRoute(Route.Matches) },
                        navigateBack = { navController.popBackStack() },
                        setTopBarState = { setTopBarState(it, Route.Registration.route) }
                    )
                }
                composable(Route.Matches.route) {
                    updateTopBar()
                    MatchesScreen(
                        navigateToProfile = { profileId ->
                            Log.d("Navigation", "Navigate to profile $profileId")
                            navigateToRouteWithArgs(
                                Route.Profile.createRoute(profileId)
                            )
                        },
                    )
                }
                composable(Route.MyProfile.route) {
                    ProfileScreen(
                        userId = null,
                        isCurrentUser = true,
                        setTopBarState = { setTopBarState(it, Route.MyProfile.route) },
                        goToMessages = { chatId ->
                            /* do nothing */
                            Log.d(
                                "Navigation",
                                "Impossible: go to messages with chatId $chatId from MyProfile"
                            )
                        },
                        goToEditProfile = {
                            navigateToRoute(Route.EditProfile)
                        },
                        goToSettings = {
                            navigateToRoute(Route.Settings)
                        },
                        goToAddPost = {
                            navigateToRoute(Route.NewPost)
                        },
                        goToMatches = {
                            /* do nothing */
                            Log.d(
                                "Navigation",
                                "Impossible: go to matches from MyProfile"
                            )
                        },
                        goToProfile = { profileId ->
                            Log.d("Navigation", "Navigate to profile $profileId from MyProfile")
                            navigateToRouteWithArgs(
                                Route.Profile.createRoute(profileId)
                            )
                        }
                    )
                }
                composable(Route.ChatsList.route) {
                    ChatsListScreen(
                        navigateToChat = { chatId ->
                            Log.d("Navigation", "Navigate to chat $chatId")
                            navigateToRouteWithArgs(
                                Route.Chat.createRoute(chatId)
                            )
                        },
                        setTopBarState = { setTopBarState(it, Route.ChatsList.route) },
                    )
                }
                composable(Route.Profile.route) { backStackEntry ->
                    val profileId =
                        backStackEntry.arguments?.getString("profile_id")
                    ProfileScreen(
                        userId = profileId,
                        isCurrentUser = false,
                        setTopBarState = { setTopBarState(it, Route.Profile.route) },
                        goToMessages = { chatId ->
                            navigateToRouteWithArgs(Route.Chat.createRoute(chatId = chatId))
                        },
                        goToEditProfile = {
                            /* do nothing */
                            Log.d(
                                "Navigation",
                                "Impossible: go to edit profile from other user's profile"
                            )
                        },
                        goToSettings = {
                            /* do nothing */
                            Log.d(
                                "Navigation",
                                "Impossible: go to settings from other user's profile"
                            )
                        },
                        goToAddPost = {
                            /* do nothing */
                            Log.d(
                                "Navigation",
                                "Impossible: go to add post from other user's profile"
                            )
                        },
                        goToMatches = {
                            navigateToRoute(Route.Matches)
                        },
                        goToProfile = { profileId ->
                            /* do nothing */
                            Log.d(
                                "Navigation",
                                "Impossible: go to profile $profileId from profile $profileId"
                            )
                        }
                    )
                }
                composable(Route.Chat.route) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chat_id")
                    ChatScreen(
                        profileId = chatId,
                        setTopBarState = { setTopBarState(it, Route.Chat.route) },
                        goToProfile = { profileId ->
                            Log.d("Navigation", "Navigate to profile $profileId from chat")
                            navigateToRouteWithArgs(
                                Route.Profile.createRoute(profileId)
                            )
                        },
                    )
                }
                composable(Route.Filters.route) {
                    updateTopBar()
                    FiltersScreen(
                        navigateToMatches = {
                            navigateToRoute(Route.Matches)
                        },
                    )
                }
                composable(Route.Feed.route) {
                    updateTopBar()
                    FeedScreen(
                        navigateToPost = { postId ->
                            Log.d("Navigation", "Navigate to post $postId from feed")
                            navigateToRouteWithArgs(
                                Route.Post.createRoute(postId)
                            )
                        }
                    )
                }
                composable(Route.EditProfile.route) {
                    EditProfileScreen(
                        setTopBarState = { setTopBarState(it, Route.EditProfile.route) },
                        navigateToAddFandom = {
                            navigateToRoute(Route.AddFandom)
                        },
                        navigateToMyProfile = {
                            navigateToRoute(Route.MyProfile)
                        },
                    )
                }
                composable(Route.Settings.route) {
                    SettingsScreen(
                        setTopBarState = { setTopBarState(it, Route.Settings.route) },
                        navigateToIntro = { navigateToRoute(Route.Intro) },
                    )
                }
                composable(Route.AddFandom.route) {
                    updateTopBar()
                    AddFandomScreen(
                        navigateBack = { navController.popBackStack() },
                    )
                }
                composable(Route.NewPost.route) {
                    NewPostScreen(
                        navigateToPreviousScreen = { navController.popBackStack() },
                        setTopBarState = { setTopBarState(it, Route.NewPost.route) },
                    )
                }
                composable(Route.PasswordRecovery.route) {
                    updateTopBar()
                    PasswordRecoveryScreen(
                        navigateToAuthorization = { navigateToRoute(Route.Authorization) }
                    )
                }
                composable(Route.Post.route) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("post_id")
                    PostScreen(
                        postId = postId,
                        setTopBarState = { setTopBarState(it, Route.Post.route) },
                        goToProfile = { profileId ->
                            Log.d("Navigation", "Navigate to profile $profileId from post")
                            navigateToRouteWithArgs(
                                Route.Profile.createRoute(profileId)
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun String.canShowBottomBar(): Boolean {
    return this !in listOf(
        Route.Authorization.route,
        Route.Registration.route,
        Route.Intro.route,
        Route.Chat.route,
        Route.Filters.route,
        Route.PasswordRecovery.route,
        Route.EditProfile.route,
        Route.Post.route,
    )
}
