package ru.hse.fandomatch.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.ui.composables.RawImageOrPlaceholder

@Composable
internal fun Message(
    modifier: Modifier,
    message: Message,
    needsTail: Boolean,
    onImageClicked: (List<String>, Int) -> Unit,
    otherSidePadding: Dp = 40.dp,
) {
    Box(
        contentAlignment = if (message.isFromThisUser) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
    ) {
        if (message.isFromThisUser) OutgoingMessageBox(
            modifier = Modifier.padding(start = otherSidePadding),
            needsTail = needsTail,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        ) { MessageContent(message, onImageClicked) }
        else IncomingMessageBox(
            modifier = Modifier.padding(end = otherSidePadding),
            needsTail = needsTail,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        ) { MessageContent(message, onImageClicked) }
    }
}

@Composable
private fun IncomingMessageBox(
    modifier: Modifier = Modifier,
    triangleSize: Dp = 10.dp,
    needsTail: Boolean,
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: @Composable BoxScope.() -> Unit,
) {
    val triangleSizePx = LocalDensity.current.run { triangleSize.toPx() }
    val cornerRadiusPx = LocalDensity.current.run { cornerRadius.toPx() }

    val shape = remember(triangleSize, cornerRadius, needsTail) {
        GenericShape { size, _ ->
            val tail = Path().apply {
                if (!needsTail) return@apply
                moveTo(0f, size.height)
                lineTo(triangleSizePx * 2, size.height)
                lineTo(triangleSizePx * 2, size.height - triangleSizePx * 2)
                lineTo(0f, size.height)
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

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(start = triangleSize),
        content = content
    )
}

@Composable
private fun OutgoingMessageBox(
    modifier: Modifier = Modifier,
    triangleSize: Dp = 10.dp,
    needsTail: Boolean,
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    paddingValues: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.() -> Unit,
) {
    val triangleSizePx = LocalDensity.current.run { triangleSize.toPx() }
    val cornerRadiusPx = LocalDensity.current.run { cornerRadius.toPx() }

    val shape = remember(triangleSize, cornerRadius) {
        GenericShape { size, _ ->
            val tail = Path().apply {
                if (!needsTail) return@apply
                moveTo(size.width, size.height)
                lineTo(size.width - triangleSizePx * 2, size.height)
                lineTo(size.width - triangleSizePx * 2, size.height - triangleSizePx * 2)
                lineTo(size.width, size.height)
                close()
            }

            val body = Path().apply {
                val rect = Rect(Offset(0f, 0f), Offset(size.width - triangleSizePx, size.height))
                addRoundRect(RoundRect(rect, cornerRadiusPx, cornerRadiusPx))
            }

            val path = Path().apply {
                op(body, tail, PathOperation.Union)
            }

            addPath(path)
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(end = triangleSize)
            .padding(paddingValues),
        content = content
    )
}

@Composable
private fun MessageContent(
    message: Message,
    onImageClicked: (List<String>, Int) -> Unit,
) {
    Column {
        if (message.imageUrls.isNotEmpty()) {
            MessageImagesGrid(
                imageUrls = message.imageUrls,
                modifier = Modifier.padding(bottom = 4.dp),
                onImageClicked = onImageClicked,
            )
        }
        if (message.content.isNotEmpty()) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = if (message.imageUrls.isEmpty()) 4.dp else 0.dp,
                    bottom = 4.dp
                )
            )
        }
    }
}

@Composable
private fun MessageImagesGrid(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    onImageClicked: (List<String>, Int) -> Unit,
    height: Dp = 300.dp
) {
    val maxImages = imageUrls.take(5)
    val imageModifier = Modifier
        .padding(2.dp)
        .clip(RoundedCornerShape(8.dp))

    val context = LocalContext.current

    when (maxImages.size) {
        1 -> {
            Box(modifier = modifier) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier
                        .height(height)
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
            }
        }

        2 -> {
            Row(
                modifier = Modifier.height(height)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )

                RawImageOrPlaceholder(
                    url = maxImages[1],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                        .weight(1f)
                        .clickable {
                            onImageClicked(imageUrls, 1)
                        }
                )
            }
        }

        3 -> {
            Row(
                modifier = Modifier.height(height)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    modifier = imageModifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RawImageOrPlaceholder(
                        url = maxImages[1],
                        placeholderId = R.drawable.ic_account_placeholder, // todo
                        context = context,
                        modifier = imageModifier
                            .weight(1f)
                            .clickable {
                                onImageClicked(imageUrls, 1)
                            }
                    )

                    RawImageOrPlaceholder(
                        url = maxImages[2],
                        placeholderId = R.drawable.ic_account_placeholder, // todo
                        context = context,
                        modifier = imageModifier
                            .weight(1f)
                            .clickable {
                                onImageClicked(imageUrls, 2)
                            }
                    )
                }
            }
        }

        4 -> {
            Row(
                modifier = Modifier.height(200.dp)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    modifier = imageModifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RawImageOrPlaceholder(
                        url = maxImages[1],
                        placeholderId = R.drawable.ic_account_placeholder, // todo
                        context = context,
                        modifier = imageModifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable {
                                onImageClicked(imageUrls, 1)
                            }
                    )

                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        RawImageOrPlaceholder(
                            url = maxImages[2],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 2)
                                }
                        )

                        RawImageOrPlaceholder(
                            url = maxImages[3],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 3)
                                }
                        )
                    }
                }
            }
        }

        5 -> {
            Row(
                modifier = Modifier.height(200.dp)
            ) {
                RawImageOrPlaceholder(
                    url = maxImages[0],
                    placeholderId = R.drawable.ic_account_placeholder, // todo
                    context = context,
                    modifier = imageModifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onImageClicked(imageUrls, 0)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        RawImageOrPlaceholder(
                            url = maxImages[1],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            modifier = imageModifier
                                .weight(1f)
                                .clickable {
                                    onImageClicked(imageUrls, 1)
                                }
                        )

                        RawImageOrPlaceholder(
                            url = maxImages[2],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            modifier = imageModifier
                                .weight(1f)
                                .clickable {
                                    onImageClicked(imageUrls, 2)
                                }
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        RawImageOrPlaceholder(
                            url = maxImages[3],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 3)
                                }
                        )

                        RawImageOrPlaceholder(
                            url = maxImages[4],
                            placeholderId = R.drawable.ic_account_placeholder, // todo
                            context = context,
                            modifier = imageModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    onImageClicked(imageUrls, 4)
                                }
                        )
                    }
                }
            }
        }
    }
}
