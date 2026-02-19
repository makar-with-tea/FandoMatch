package ru.hse.fandomatch.ui.navigation

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
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
import ru.hse.fandomatch.ui.authorization.AuthorizationScreen
import ru.hse.fandomatch.ui.chat.ChatScreen
import ru.hse.fandomatch.ui.chatslist.ChatsListScreen
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.feed.FeedScreen
import ru.hse.fandomatch.ui.filters.FiltersScreen
import ru.hse.fandomatch.ui.intro.IntroScreen
import ru.hse.fandomatch.ui.matches.MatchesScreen
import ru.hse.fandomatch.ui.myprofile.ProfileScreen
import ru.hse.fandomatch.ui.registration.RegistrationScreen
import ru.hse.fandomatch.ui.utils.orFalse

sealed class Route(val route: String) {
    data object Authorization : Route("authorization")
    data object Chat : Route("chat/{chat_id}") {
        fun createRoute(chatId: Long?): String {
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
        fun createRoute(profileId: Long): String {
            return "profile/$profileId"
        }
    }

    data object Registration : Route("registration")
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
                Route.Intro.route, Route.Registration.route, Route.Authorization.route -> null
                else -> R.string.empty_string
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
        Box(modifier = Modifier.padding(paddingValues)) {
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
                        navigateToMatches = { navigateToRoute(Route.Matches) }
                    )
                }
                composable(Route.Registration.route) {
                    updateTopBar()
                    RegistrationScreen(
                        navigateToMatches = { navigateToRoute(Route.Matches) },
                        navigateBack = { navController.popBackStack() },
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
                        setTopBarState = { topBarState.value = it },
                        goToMessages = { chatId ->
                            navigateToRouteWithArgs(Route.Chat.createRoute(chatId = chatId))
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
                        setTopBarState = { topBarState.value = it },
                    )
                }
                composable(Route.Profile.route) { backStackEntry ->
                    val profileId =
                        backStackEntry.arguments?.getString("profile_id")?.toLongOrNull() ?: -1L
                    ProfileScreen(
                        userId = profileId,
                        setTopBarState = { topBarState.value = it },
                        goToMessages = { chatId ->
                            navigateToRouteWithArgs(Route.Chat.createRoute(chatId = chatId))
                        }
                    )
                }
                composable(Route.Chat.route) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chat_id")?.toLongOrNull()
                    ChatScreen(
                        userId = chatId,
                        setTopBarState = { topBarState.value = it },
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
                    val context = LocalContext.current
                    FeedScreen(
                        navigateToPost = { postId ->
                            // todo
                            Toast.makeText(
                                context,
                                "Clicked post $postId",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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
        Route.Chat.route, // todo check if it works
        Route.Filters.route,
    )
}
