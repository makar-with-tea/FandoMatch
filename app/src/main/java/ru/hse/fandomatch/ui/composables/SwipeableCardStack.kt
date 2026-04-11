package ru.hse.fandomatch.ui.composables

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.hse.fandomatch.domain.model.ProfileCard
import kotlin.math.roundToInt

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

    val returnAnim = tween<Float>(durationMillis = 180)

    Card(
        modifier = modifier
            .zIndex(if (isTop) 1f else 0f)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .rotate(rotation.value)
            .pointerInput(isTop) {
                if (!isTop) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        onDragEndNonComposable(
                            scope = scope,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            rotation = rotation,
                            returnAnim = returnAnim,
                            onSwipedRight = onLike,
                            onSwipedLeft = onDislike
                        )
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            rotation.snapTo((offsetX.value / 25f).coerceIn(-20f, 20f))
                        }
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

private fun onDragEndNonComposable(
    scope: CoroutineScope,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    rotation: Animatable<Float, *>,
    returnAnim: AnimationSpec<Float>,
    onSwipedRight: () -> Unit,
    onSwipedLeft: () -> Unit
) {
    when {
        offsetX.value > SWIPE_THRESHOLD -> {
            launchSwipeOut(scope, offsetX, rotation, toRight = true) { onSwipedRight() }
        }
        offsetX.value < -SWIPE_THRESHOLD -> {
            launchSwipeOut(scope, offsetX, rotation, toRight = false) { onSwipedLeft() }
        }
        else -> {
            scope.launch {
                offsetX.animateTo(0f, returnAnim)
                offsetY.animateTo(0f, returnAnim)
                rotation.animateTo(0f, returnAnim)
            }
        }
    }
}

private fun launchSwipeOut(
    scope: CoroutineScope,
    offsetX: Animatable<Float, *>,
    rotation: Animatable<Float, *>,
    toRight: Boolean,
    after: () -> Unit
) {
    val targetX = if (toRight) FLING_X else -FLING_X
    val targetRot = if (toRight) 25f else -25f
    scope.launch {
        offsetX.animateTo(targetX, tween(160))
        rotation.animateTo(targetRot, tween(160))
        after()
        offsetX.snapTo(0f)
        rotation.snapTo(0f)
    }
}

private const val SWIPE_THRESHOLD = 160f
private const val FLING_X = 1000f
