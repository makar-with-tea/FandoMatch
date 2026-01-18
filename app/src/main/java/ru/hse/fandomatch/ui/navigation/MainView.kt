package ru.hse.fandomatch.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.authorization.AuthorizationScreen
import ru.hse.fandomatch.ui.chat.ChatScreen
import ru.hse.fandomatch.ui.chatslist.ChatsListScreen
import ru.hse.fandomatch.ui.intro.IntroScreen
import ru.hse.fandomatch.ui.matches.MatchesScreen
import ru.hse.fandomatch.ui.myprofile.MyProfileScreen
import ru.hse.fandomatch.ui.registration.RegistrationScreen
import ru.hse.fandomatch.ui.utils.orFalse

sealed class Route(val route: String) {
    data object Intro: Route("intro")
    data object Authorization : Route("authorization")

    data object Registration : Route("registration")

    data object MyProfile: Route("my_profile")

    data object Matches: Route("matches")

    data object Profile: Route("profile/{profile_id}") {
        fun createRoute(profileId: Long): String {
            return "profile/$profileId"
        }
    }

    data object Feed: Route("feed")

    data object ChatsList: Route("chats_list")

    data object Chat: Route("chat/{chat_id}") {
        fun createRoute(chatId: Long): String {
            return "chat/$chatId"
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

    Scaffold(
        topBar = {
            val screenTitle = when (currentRoute) {
                Route.Authorization.route -> stringResource(id = R.string.authorization_title)
                Route.MyProfile.route -> stringResource(id = R.string.my_profile_title)
                Route.Matches.route -> stringResource(id = R.string.matches_title)
                Route.ChatsList.route -> stringResource(id = R.string.chats_list_title)
                else -> null
            }

            val endIcons = when (currentRoute) {
                Route.Matches.route -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_filters,
                        onClick = { /* TODO */ },
                        description = stringResource(id = R.string.filters_icon_description)
                    )
                )

                Route.MyProfile.route -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_edit,
                        onClick = { /* TODO */ },
                        description = stringResource(id = R.string.edit_profile_icon_description)
                    ),
                    EndIconState(
                        iconId = R.drawable.ic_settings,
                        onClick = { /* TODO */ },
                        description = stringResource(id = R.string.settings_icon_description)
                    )
                )

                Route.ChatsList.route -> listOf(
                    EndIconState(
                        iconId = R.drawable.ic_search,
                        onClick = { /* TODO */ },
                        description = stringResource(id = R.string.search_icon_description)
                    ),
                )

                else -> listOf()
            }

            screenTitle?.let {
                TopBar(
                    state = TopBarState(
                        title = screenTitle,
                        endIcons = endIcons,
                    ),
                    onBackClick = { navController.popBackStack() }
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
                    navigateToFeed = { /* TODO */ },
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = Route.Intro.route) {
                composable(Route.Intro.route) {
                    IntroScreen(
                        navigateToMatches = { navigateToRoute(Route.Matches) },
                        navigateToLogin = { navigateToRoute(Route.Authorization) },
                        navigateToRegistration = { navigateToRoute(Route.Registration) },
                    )
                }
                composable(Route.Authorization.route) {
                    AuthorizationScreen(
                        navigateToMatches = { navigateToRoute(Route.Matches) }
                    )
                }
                composable(Route.Registration.route) {
                    RegistrationScreen(
                        navigateToMatches = { navigateToRoute(Route.Matches) },
                        navigateBack = { navController.popBackStack() },
                    )
                }
                composable(Route.Matches.route) {
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
                    MyProfileScreen()
                }
                composable(Route.ChatsList.route) {
                    ChatsListScreen(
                        navigateToChat = { chatId ->
                            Log.d("Navigation", "Navigate to chat $chatId")
                            navigateToRouteWithArgs(
                                Route.Chat.createRoute(chatId)
                            )
                        }
                    )
                }
                composable(Route.Profile.route) { backStackEntry ->
                    val profileId = backStackEntry.arguments?.getString("profile_id")?.toIntOrNull()
                    if (profileId != null) {
                        Text("Profile $profileId TODO")
                    } else {
                        Text("Profile not found")
                    }
                }
                composable(Route.Chat.route) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chat_id")?.toLongOrNull()
                    ChatScreen(
                        chatId = chatId,
                        onBackClicked = { navController.popBackStack() }
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
    )
}
