package ru.hse.fandomatch.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import ru.hse.fandomatch.R


@Composable
fun BottomBar(
    navigateToMatches: () -> Unit,
    navigateToMyProfile: () -> Unit,
    navigateToChats: () -> Unit,
    navigateToFeed: () -> Unit,
    currentRoute: String?
) {
    NavigationBar(
        modifier = Modifier.height(100.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.tertiary,
            unselectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        NavigationBarItem(
            selected = currentRoute == Route.Matches.route,
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (currentRoute == Route.Matches.route)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_cards_stack_star),
                        contentDescription = stringResource(R.string.matches_icon_description)
                    )
                }
            },
            colors = colors,
            onClick = {
                navigateToMatches()
                Log.d("Navigation", "BottomNav to Matches from $currentRoute")
            }
        )

        NavigationBarItem(
            selected = currentRoute == Route.Feed.route,
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (currentRoute == Route.Feed.route)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_feed),
                        contentDescription = stringResource(R.string.feed_icon_description)
                    )
                }
            },
            colors = colors,
            onClick = {
                navigateToFeed()
                Log.d("Navigation", "BottomNav to Feed from $currentRoute")
            }
        )

        NavigationBarItem(
            selected = currentRoute == Route.ChatsList.route,
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (currentRoute == Route.ChatsList.route)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_message),
                        contentDescription = stringResource(R.string.chats_list_icon_description)
                    )
                }
            },
            colors = colors,
            onClick = {
                navigateToChats()
                Log.d("Navigation", "BottomNav to ChatsList from $currentRoute")
            }
        )

        NavigationBarItem(
            selected = currentRoute == Route.MyProfile.route,
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (currentRoute == Route.MyProfile.route)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_account_box),
                        contentDescription = stringResource(R.string.my_profile_icon_description)
                    )
                }
            },
            colors = colors,
            onClick = {
                navigateToMyProfile()
                Log.d("Navigation", "BottomNav to Account from $currentRoute")
            }
        )
    }
}
