package ru.hse.fandomatch.ui.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Comment
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.epochMillisToTimeString

@Composable
fun FeedPost(
    userName: String,
    userLogin: String?,
    userAvatarUrl: String?,
    postDate: String,
    postText: String?,
    imageUrls: List<String>,
    areReactionsAvailable: Boolean,
    likeCount: Int,
    commentCount: Int,
    fandoms: List<Fandom>,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onPostClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 4.dp)
    ) {
        Box {
            Column {
                AvatarAndNameBlock(
                    name = userName,
                    avatarUrl = userAvatarUrl,
                    login = userLogin,
                    backgroundColor = backgroundColor,
                    avatarSize = 36.dp
                )
                if (imageUrls.isNotEmpty()) {
                    ImagesGrid(
                        imageUrls = imageUrls,
                        onImageClicked = { _, _ -> },
                        minHeight = 100.dp,
                        maxHeight = 300.dp
                    )
                }
                postText?.let {
                    ExpandableText(
                        text = it,
                        backgroundColor = backgroundColor,
                        collapsedMaxLine = 5,
                    )
                }
                if (fandoms.isNotEmpty()) {
                    FandomCarouselWithDropdown(
                        fandoms = fandoms,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(8.dp)
                    .clickable { onPostClick() }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = postDate,
                style = MaterialTheme.typography.bodySmall
            )
            Box(modifier = Modifier.weight(1f))
            if (areReactionsAvailable) {
                Row(
                    modifier = Modifier
                        .clickable { onPostClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = stringResource(R.string.comment_button_description)
                    )

                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = commentCount.toString()
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { onLikeClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.like_button_description),
                        tint = if (isLiked) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                    )
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = likeCount.toString()
                    )
                }
            }
        }
    }
}

@Composable
fun FullPost(
    postDate: String,
    postText: String?,
    imageUrls: List<String>,
    areReactionsAvailable: Boolean,
    likeCount: Int,
    commentCount: Int,
    fandoms: List<Fandom>,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 4.dp)
    ) {
        if (imageUrls.isNotEmpty()) {
            ImagesGrid(
                imageUrls = imageUrls,
                onImageClicked = { urls, index -> onImageClick(urls, index) },
                minHeight = 100.dp,
                maxHeight = 300.dp
            )
        }
        postText?.let {
            ExpandableText(
                text = it,
                backgroundColor = backgroundColor,
                collapsedMaxLine = 5,
            )
        }
        if (fandoms.isNotEmpty()) {
            FandomCarouselWithDropdown(
                fandoms = fandoms,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = postDate,
                style = MaterialTheme.typography.bodySmall
            )
            Box(modifier = Modifier.weight(1f))
            if (areReactionsAvailable) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = stringResource(R.string.comment_button_description)
                    )

                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = commentCount.toString()
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { onLikeClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.like_button_description),
                        tint = if (isLiked) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                    )
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = likeCount.toString()
                    )
                }
            }
        }
    }
}

@Composable
fun PostComment(
    comment: Comment,
    modifier: Modifier = Modifier,
    triangleSize: Dp = 10.dp,
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    val triangleSizePx = LocalDensity.current.run { triangleSize.toPx() }
    val cornerRadiusPx = LocalDensity.current.run { cornerRadius.toPx() }

    val shape = remember(triangleSize, cornerRadius) {
        GenericShape { size, _ ->
            val tail = Path().apply {
                moveTo(0f, size.height / 2)
                lineTo(triangleSizePx * 2, size.height / 2 - triangleSizePx)
                lineTo(triangleSizePx * 2, size.height / 2 + triangleSizePx)
                lineTo(0f, size.height / 2)
                close()
            }

            val body = Path().apply {
                val rect = Rect(Offset(triangleSizePx, 0f), Offset(size.width, size.height))
                addRoundRect(RoundRect(rect, cornerRadiusPx, cornerRadiusPx))
            }

            val path = Path().apply {
                op(body, tail, PathOperation.Union)
            }

            addPath(path)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RawImageOrPlaceholder(
            modifier = Modifier
                .padding(start = 4.dp, top = 2.dp, bottom = 2.dp, end = 2.dp)
                .size(24.dp)
                .clip(CircleShape),
            url = comment.authorAvatarUrl,
            placeholderId = R.drawable.ic_account_placeholder,
            context = LocalContext.current,
        )

        Box(
            modifier = modifier
                .clip(shape)
                .background(backgroundColor)
                .padding(start = triangleSize)
        ) {
            Column {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    text = comment.authorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = comment.timestamp.epochMillisToTimeString(),
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}


@Composable
fun LoadingPosts() {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, 0, LinearEasing)
            )
            animatable.snapTo(0f)
        }
    }

    val tan = remember { 0.26795f } // tan(15 degrees)
    val screenHeight = with (LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val globalY = screenHeight * animatable.value
    val globalX = tan * globalY

    val endY = screenHeight + globalY
    val endX = tan * endY

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
    ) {
        repeat(5) {
            SkeletonView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 4.dp)
                    .clip(shape = RoundedCornerShape(12.dp)),
                globalX = globalX,
                globalY = globalY,
                endX = endX,
                endY = endY,
            )
        }
    }
}
