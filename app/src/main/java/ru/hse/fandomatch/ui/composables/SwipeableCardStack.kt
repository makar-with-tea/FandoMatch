package ru.hse.fandomatch.ui.composables

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.hse.fandomatch.domain.model.ProfileCard
import kotlin.math.abs

@Composable
fun SwipeableCardStack(
    profiles: List<ProfileCard>,
    onLike: (String) -> Unit,
    onDislike: (String) -> Unit,
    onCardClick: (String) -> Unit,
) {
    Log.i("SwipeableCardStack", "Rendering SwipeableCardStack with: ${profiles.map { it.name }}")
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        profiles.forEachIndexed { index, profile ->
            val isTop = index == profiles.lastIndex
            SwipeableCard(
                profile = profile,
                isTop = isTop,
                onLike = { onLike(profile.id) },
                onDislike = { onDislike(profile.id) },
                onClick = { onCardClick(profile.id) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }
    }
}
@Composable
private fun SwipeableCard(
    profile: ProfileCard,
    isTop: Boolean,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .zIndex(if (isTop) 1f else 0f)
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = rotation.value
                transformOrigin = TransformOrigin(0.5f, 0.9f)
            }
            .pointerInput(isTop) {
                if (!isTop) return@pointerInput

                var dragStartY = 0f
                var cardHeight = 1f
                val velocityTracker = VelocityTracker()

                detectDragGestures(
                    onDragStart = { startOffset ->
                        dragStartY = startOffset.y
                        cardHeight = size.height.toFloat().coerceAtLeast(1f)
                        velocityTracker.resetTracking()
                        velocityTracker.addPosition(0L, startOffset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)

                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y * 0.15f)

                            val sign = if (dragStartY > cardHeight / 2f) -1f else 1f
                            val rot = (offsetX.value / 24f) * sign
                            rotation.snapTo(rot.coerceIn(-18f, 18f))
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            rotation.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    },
                    onDragEnd = {
                        val vx = velocityTracker.calculateVelocity().x
                        handleSwipeEnd(
                            scope = scope,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            rotation = rotation,
                            velocityX = vx,
                            onSwipedRight = onLike,
                            onSwipedLeft = onDislike
                        )
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        SwipeableCardContent(
            profile = profile,
            isTop = isTop,
            onLike = onLike,
            onDislike = onDislike,
            onClick = onClick,
        )
    }
}

private fun handleSwipeEnd(
    scope: CoroutineScope,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    rotation: Animatable<Float, *>,
    velocityX: Float,
    onSwipedRight: () -> Unit,
    onSwipedLeft: () -> Unit
) {
    val hasHorizontalIntent = abs(offsetX.value) >= abs(offsetY.value)
    val shouldSwipeRight = hasHorizontalIntent && (
        offsetX.value > SWIPE_DISTANCE_THRESHOLD ||
            (offsetX.value > 0f && velocityX > SWIPE_VELOCITY_THRESHOLD)
        )
    val shouldSwipeLeft = hasHorizontalIntent && (
        offsetX.value < -SWIPE_DISTANCE_THRESHOLD ||
            (offsetX.value < 0f && velocityX < -SWIPE_VELOCITY_THRESHOLD)
        )

    when {
        shouldSwipeRight -> launchSwipeOut(
            scope = scope,
            offsetX = offsetX,
            offsetY = offsetY,
            rotation = rotation,
            toRight = true,
            after = onSwipedRight
        )
        shouldSwipeLeft -> launchSwipeOut(
            scope = scope,
            offsetX = offsetX,
            offsetY = offsetY,
            rotation = rotation,
            toRight = false,
            after = onSwipedLeft
        )
        else -> {
            scope.launch {
                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                rotation.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
            }
        }
    }
}

private fun launchSwipeOut(
    scope: CoroutineScope,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    rotation: Animatable<Float, *>,
    toRight: Boolean,
    after: () -> Unit
) {
    val targetX = if (toRight) FLING_X else -FLING_X
    val targetRot = if (toRight) 24f else -24f

    scope.launch {
        kotlinx.coroutines.coroutineScope {
            launch { offsetX.animateTo(targetX, tween(170)) }
            launch { offsetY.animateTo(offsetY.value + 30f, tween(170)) }
            launch { rotation.animateTo(targetRot, tween(170)) }
        }
        after()
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
        rotation.snapTo(0f)
    }
}

private const val SWIPE_DISTANCE_THRESHOLD = 140f
private const val SWIPE_VELOCITY_THRESHOLD = 1400f
private const val FLING_X = 1200f
