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
import ru.hse.fandomatch.ui.intro.IntroScreen
import ru.hse.fandomatch.ui.registration.RegistrationScreen
import ru.hse.fandomatch.ui.utils.orFalse

sealed class Route(val route: String) {
    data object Intro: Route("intro")
    data object Authorization : Route("authorization")

    data object Registration : Route("registration")

    data object Account: Route("account")

    data object Matches: Route("matches")
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

    // todo go back from the first screen??

    Scaffold(
        topBar = {
            val screenTitle = when (currentRoute) {
                Route.Authorization.route -> stringResource(id = R.string.authorization_title)
                Route.Account.route -> stringResource(id = R.string.my_profile_title)
                Route.Matches.route -> stringResource(id = R.string.matches_title)
                else -> null
            }

            screenTitle?.let {
                TopBar(
                    state = TopBarState.Title(
                        title = screenTitle,
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
                    navigateToAccount = { navigateToRoute(Route.Account) },
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
                    Text("Matches TODO")
                }
                composable(Route.Account.route) {
                    Text("Account TODO")
                }
            }
        }
    }
}

private fun String.canShowBottomBar(): Boolean {
    return this != Route.Authorization.route
            && this != Route.Registration.route
            && this != Route.Intro.route
}
