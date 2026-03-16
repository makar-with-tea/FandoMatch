package ru.hse.fandomatch.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.tooling.preview.Preview
import ru.hse.fandomatch.ui.theme.FandoMatchTheme

@Composable
fun SkeletonView(
    modifier: Modifier,
    globalX: Float,
    globalY: Float,
    endX: Float,
    endY: Float,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    var globalPosition by remember { mutableStateOf(Offset.Zero) }
    val colors = remember {
        listOf(
            color.copy(alpha = 0.05f),
            color.copy(alpha = 0.08f),
            color.copy(alpha = 0.1f),
            color.copy(alpha = 0.08f),
            color.copy(alpha = 0.05f)
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned {
                globalPosition = it.positionInWindow()
            }
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(
                            x = -globalPosition.x + globalX,
                            y = -globalPosition.y + globalY
                        ),
                        end = Offset(
                            x = -globalPosition.x + endX,
                            y = -globalPosition.y + endY
                        ),
                        tileMode = TileMode.Repeated
                    ),
                )
            }
    )
}

@Preview
@Composable
fun SkeletonViewPreview() {
    FandoMatchTheme {
        SkeletonView(
            modifier = Modifier,
            globalX = 0f,
            globalY = 0f,
            endX = 100f,
            endY = 100f
        )
    }
}
