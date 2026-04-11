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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Gender
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(enabled = isTop) { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomStart
        ) {
            RawImageOrPlaceholder(
                url = profile.avatarUrl,
                context = LocalContext.current,
                placeholderId = R.drawable.ic_account_placeholder,
                modifier = modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp)),
            )

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(top = 8.dp, end = 12.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    text = nameAndAgeString(profile.name, profile.age)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(
                            end = 12.dp,
                            top = if (profile.city != null
                                || profile.gender != Gender.NOT_SPECIFIED) 0.dp else 4.dp)
                ) {
                    if (profile.city != null || profile.gender != Gender.NOT_SPECIFIED) {
                        CityAndGenderText(
                            city = profile.city,
                            gender = profile.gender,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        )
                    }
                    if (!profile.description.isNullOrBlank()) {
                        Text(
                            text = profile.description.orEmpty(),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (profile.fandoms.isNotEmpty()) {
                    FandomGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topEnd = if (profile.description.isNullOrBlank()
                                        && profile.city == null
                                        && profile.gender == Gender.NOT_SPECIFIED) 40.dp else 0.dp
                                )
                            )
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(end = 8.dp, top = 4.dp),
                        fandoms = profile.fandoms,
                        maxLines = 2,
                    )
                }
            }
        }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(start = 14.dp, end = 14.dp, top = 6.dp),
                    text = "${profile.compatibilityPercentage}%",
                    color = MaterialTheme.colorScheme.onPrimary,
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
