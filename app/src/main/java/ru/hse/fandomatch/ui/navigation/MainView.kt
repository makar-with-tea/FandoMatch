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

sealed class Route(val route: String) {
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

    val navigateToAccount = {
        navController.navigate(Route.Account.route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        topBar = {
            val screenTitle = when (currentRoute) {
                Route.Authorization.route -> stringResource(id = R.string.authorization_title)
                Route.Registration.route -> stringResource(id = R.string.registration_title)
                Route.Account.route -> stringResource(id = R.string.my_profile_title)
                else -> ""
            }

            if (currentRoute != Route.Authorization.route && currentRoute != Route.Registration.route) {
                TopBar(
                    title = screenTitle,
                    onBackClick = { navController.popBackStack() }
                )
            }
        },
        bottomBar = {
            Log.d("SetUpNavHost", "Current route: $currentRoute")
            if (currentRoute != Route.Authorization.route && currentRoute != Route.Registration.route) {
                BottomNavigationBar(
                    navigateToAccount = navigateToAccount,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = Route.Authorization.route) {
                composable(Route.Authorization.route) {
                    AuthorizationScreen(
                        navigateToRegistration = {
                            navController.navigate(Route.Registration.route) {
                                popUpTo(Route.Authorization.route) {
                                    inclusive = true
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            Log.d("Navigation", "Navigate to Registration from Authorization")
                        },
                        navigateToMatches = {
                            navController.navigate(Route.Matches.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                            Log.d("Navigation", "Navigate to Matches from Authorization")
                        },
                    )
                }
                composable(Route.Registration.route) {
                    Text("Registration TODO")
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