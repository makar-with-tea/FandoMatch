package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.Message
import ru.hse.fandomatch.utils.epochMillisToTimeString

@Composable
internal fun Message(
    modifier: Modifier,
    message: Message,
    needsTail: Boolean,
    onItemClicked: (List<MediaItem>, Int) -> Unit,
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
        ) { MessageContent(message, onItemClicked) }
        else IncomingMessageBox(
            modifier = Modifier.padding(end = otherSidePadding),
            needsTail = needsTail,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        ) { MessageContent(message, onItemClicked) }
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
    onItemClicked: (List<MediaItem>, Int) -> Unit,
) {
    Column {
        if (message.mediaItems.isNotEmpty()) {
            MediaItemsGrid(
                mediaItems = message.mediaItems,
                modifier = Modifier.padding(bottom = 4.dp),
                onItemClicked = onItemClicked,
                maxHeight = 300.dp
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
                    top = if (message.mediaItems.isEmpty()) 4.dp else 0.dp,
                )
            )
        }
        Text(
            text = message.timestamp.epochMillisToTimeString(),
            fontSize = 10.sp,
            lineHeight = 10.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 4.dp)
        )
    }
}
