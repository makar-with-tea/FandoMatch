package ru.hse.fandomatch.ui.chat

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.Message
import ru.hse.fandomatch.ui.composables.RawImageOrPlaceholder
import ru.hse.fandomatch.ui.composables.SkeletonView
import ru.hse.fandomatch.ui.navigation.TopBarState
import java.time.LocalDateTime

@Composable
fun ChatScreen(
    userId: Long?,
    viewModel: ChatViewModel = koinViewModel(),
    setTopBarState: (TopBarState) -> Unit,
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()
    Log.i("ChatScreen", "Rendering ChatScreen with state: ${state.value}")

    when (val action = action.value) {
        null -> {}
    }

    Log.d("ChatScreen", "State: $state")

    when (state.value) {
        is ChatState.Main -> MainState(
            state = state.value as ChatState.Main,
            setTopBarState = setTopBarState,
            onSendMessage = {
                viewModel.obtainEvent(
                    ChatEvent.SendMessage(
                        message = it,
                        timestamp = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)
                    )
                )
            }
        )

        is ChatState.Idle -> {
            IdleState()
            viewModel.obtainEvent(ChatEvent.LoadChat(userId))
        }

        is ChatState.Loading -> LoadingState()

        is ChatState.Error -> ErrorState()
    }
}

@Composable
private fun MainState(
    state: ChatState.Main,
    setTopBarState: (TopBarState) -> Unit,
    onSendMessage: (String) -> Unit,
) {
    setTopBarState(
        TopBarState(
        titleContent = @Composable{
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RawImageOrPlaceholder(
                    modifier = Modifier
                        .padding(start = 4.dp, top = 2.dp, bottom = 2.dp, end = 8.dp)
                        .size(32.dp)
                        .clip(CircleShape),
                    url = state.participantAvatarUrl,
                    placeholderId = R.drawable.ic_account_placeholder,
                    context = LocalContext.current,
                )

                Text(
                    text = state.participantName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        endIcons = emptyList(),
    )
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            items(state.messages) { (message, needsTail) ->
                Message(
                    message = message,
                    modifier = Modifier.padding(vertical = 4.dp),
                    needsTail = needsTail,
                )
            }
        }

        val newMessage: MutableState<String> = remember { mutableStateOf("") } // todo viewModel?
        OutlinedTextField(
            value = newMessage.value,
            onValueChange = { newMessage.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            placeholder = {
                Text(text = stringResource(R.string.type_a_message))
            },
            maxLines = 4,
            suffix = {
                IconButton(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (newMessage.value.isBlank())
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    enabled = newMessage.value.isNotBlank(),
                    onClick = {
                        onSendMessage(newMessage.value)
                        newMessage.value = ""
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_send),
                        contentDescription = stringResource(R.string.send_message_button_description),
                    )
                }
            }
        )
    }
}

@Composable
private fun LoadingMessages() {
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
private fun LoadingState() {
    LoadingMessages()
}

@Composable
private fun IdleState() {
    LoadingMessages()
}

@Composable
private fun ErrorState() {
    // todo
    Text("An error occurred while loading the chat.")
}
