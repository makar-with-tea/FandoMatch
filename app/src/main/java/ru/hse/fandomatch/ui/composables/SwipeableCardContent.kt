package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.nameAndAgeString

@Composable
fun SwipeableCardContent(
    profile: ProfileCard,
    isTop: Boolean,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(enabled = isTop) { onClick() }
            .padding(16.dp)
    ) {
        RawImageOrPlaceholder(
            url = profile.avatarUrl,
            context = LocalContext.current,
            placeholderId = R.drawable.ic_account_placeholder,
            modifier = modifier
                .fillMaxHeight(),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 24.sp,
                text = nameAndAgeString(profile.name, profile.age)
            )
            Text(text = profile.description.orEmpty())
            FandomGrid(
                fandoms = profile.fandoms,
                maxLines = 2,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(
                    onClick = onDislike,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_dislike),
                        contentDescription = stringResource(id = R.string.dislike_profile_description)
                    )
                }

                Text(
                    text = "${profile.compatibilityPercentage}%",
                    fontSize = 28.sp,
                    style = MaterialTheme.typography.bodyLarge,
                )

                IconButton(
                    onClick = onLike,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_like),
                        contentDescription = stringResource(id = R.string.like_profile_description)
                    )
                }
            }
        }
    }
}
