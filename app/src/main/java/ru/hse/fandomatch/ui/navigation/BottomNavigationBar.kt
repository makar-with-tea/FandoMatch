package ru.hse.fandomatch.ui.navigation

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import ru.hse.fandomatch.R


@Composable
fun BottomNavigationBar(
    navigateToAccount: () -> Unit,
    currentRoute: String?
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT


    NavigationBar(
        modifier = Modifier
            .height(if (isPortrait) 100.dp else 48.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {

        NavigationBarItem(
            selected = currentRoute == Route.Account.route,
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_account_box),
                    contentDescription = stringResource(R.string.account_icon_description)
                )
            },
            onClick = {
                navigateToAccount()
                Log.d("Navigation", "BottomNav to Account from $currentRoute")
            }
        )
    }
}
