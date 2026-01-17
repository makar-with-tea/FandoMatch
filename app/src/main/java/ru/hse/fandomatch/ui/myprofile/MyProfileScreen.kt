package ru.hse.fandomatch.ui.myprofile

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.data.mock.mockUser
import ru.hse.fandomatch.ui.composables.AvatarWithBackground
import ru.hse.fandomatch.ui.composables.ExpandableText
import ru.hse.fandomatch.ui.composables.FandomCarouselWithDropdown
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.composables.SkeletonView

@Composable
fun MyProfileScreen(
    viewModel: MyProfileViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()
    Log.i("MyProfileScreen", "Rendering MyProfileScreen with state: ${state.value}")

    when (val action = action.value) {
        null -> {}
    }

    Log.d("MyProfileScreen", "State: $state")

    when (state.value) {
        is MyProfileState.Main -> MainState(
            state = state.value as MyProfileState.Main,
        )

        is MyProfileState.Loading -> LoadingState()
    }
}

@Composable
private fun MainState(
    state: MyProfileState.Main,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        val user = state.user

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            // header of profile
            AvatarWithBackground(user.backgroundUrl, user.avatarUrl, MaterialTheme.colorScheme.tertiaryContainer)

            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MyTitle(user.name)

                FandomCarouselWithDropdown(
                    fandoms = user.fandoms
                )
                ExpandableText(
                    text = user.description.orEmpty(),
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            // posts
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.background),
            ) {
                LoadingPosts()
            }
        }
    }
}

@Composable
private fun LoadingPosts() {
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
                    .padding(vertical = 8.dp)
                    .clip(shape = RoundedCornerShape(14.dp)),
                globalX = globalX,
                globalY = globalY,
                endX = endX,
                endY = endY,
            )
        }
    }
}

@Composable
private fun LoadingState() = LoadingBlock()

@Preview(showBackground = true)
@Composable
fun MainStatePreview() {
    val mockState = MyProfileState.Main(
        user = mockUser,
        error = MyProfileState.MyProfileError.IDLE
    )

    MainState(
        state = mockState,
    )
}
