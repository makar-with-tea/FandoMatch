package ru.hse.fandomatch.ui.chat

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.MAX_NUMBER_OF_ATTACHMENTS
import ru.hse.fandomatch.R
import ru.hse.fandomatch.getBytesFromUri
import ru.hse.fandomatch.navigation.EndIconState
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.ui.composables.AttachmentsRow
import ru.hse.fandomatch.ui.composables.AvatarAndNameBlock
import ru.hse.fandomatch.ui.composables.BasicErrorState
import ru.hse.fandomatch.ui.composables.ImagesScreen
import ru.hse.fandomatch.ui.composables.Message
import ru.hse.fandomatch.ui.composables.SkeletonView
import java.time.LocalDateTime

@Composable
fun ChatScreen(
    profileId: String?,
    setTopBarState: (TopBarState?) -> Unit,
    goToProfile: (String) -> Unit,
    viewModel: ChatViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    when (val action = action.value) {
        is ChatAction.GoToProfile -> {
            goToProfile(action.profileId)
            viewModel.obtainEvent(ChatEvent.Clear)
        }

        null -> Unit
    }

    Log.d("ChatScreen", "State: $state")

    when (state.value) {
        is ChatState.Main -> MainState(
            state = state.value as ChatState.Main,
            setTopBarState = setTopBarState,
            onSendMessage = { message, images ->
                viewModel.obtainEvent(
                    ChatEvent.SendMessage(
                        message = message,
                        images = images,
                        timestamp = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)
                    )
                )
            },
            onClickProfile = {
                viewModel.obtainEvent(ChatEvent.ProfileClicked)
            }
        )

        is ChatState.Idle -> {
            IdleState()
            viewModel.obtainEvent(ChatEvent.LoadChat(profileId))
        }

        is ChatState.Loading -> LoadingState()

        is ChatState.Error -> ErrorState(
            onRetry = {
                viewModel.obtainEvent(ChatEvent.LoadChat(profileId))
            },
        )
    }
}

@Composable
private fun MainState(
    state: ChatState.Main,
    setTopBarState: (TopBarState?) -> Unit,
    onSendMessage: (String, List<ByteArray>) -> Unit,
    onClickProfile: () -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = {
                AvatarAndNameBlock(
                    name = state.participantName,
                    avatarUrl = state.participantAvatarUrl,
                    login = null,
                    onClick = { onClickProfile() },
                )
            },
        )
    )

    val context = LocalContext.current
    var attachedImages by remember { mutableStateOf(mutableListOf<ByteArray>()) }
    val pickMedia = when (MAX_NUMBER_OF_ATTACHMENTS - attachedImages.size) {
        0 -> null

        1 -> rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                getBytesFromUri(context, it)?.let { byteArray ->
                    attachedImages = (attachedImages + listOf(byteArray)).toMutableList()
                }
            }
        }

        else -> rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(
                maxItems = maxOf(MAX_NUMBER_OF_ATTACHMENTS - attachedImages.size)
            )
        ) { uris ->
            attachedImages =
                (attachedImages + uris.mapNotNull { getBytesFromUri(context, it) }).toMutableList()
        }
    }
    var imageUrlsForScreen by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentImageIndex by remember { mutableStateOf(0) }
    BackHandler(enabled = imageUrlsForScreen.isNotEmpty()) {
        imageUrlsForScreen = emptyList()
        currentImageIndex = 0
    }

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
            items(state.uiElements) { uiElement ->
                when (uiElement) {
                    is ChatUiElement.DayElement -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiElement.dateString,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }

                    is ChatUiElement.MessageElement -> {
                        Message(
                            message = uiElement.message,
                            modifier = Modifier.padding(vertical = 4.dp),
                            needsTail = uiElement.hasTail,
                            onImageClicked = { urlList, index ->
                                imageUrlsForScreen = urlList
                                currentImageIndex = index
                            }
                        )
                    }
                }
            }
        }

        AttachmentsRow(
            attachedImages = attachedImages,
            onAttachmentsChanged = {
                attachedImages = it.toMutableList()
            }
        )

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
            prefix = {
                IconButton(
                    modifier = Modifier
                        .size(24.dp),
                    enabled = attachedImages.size < MAX_NUMBER_OF_ATTACHMENTS,
                    onClick = {
                        pickMedia?.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_attach),
                        contentDescription = stringResource(R.string.attach_file_button_description),
                    )
                }
            },
            suffix = {
                IconButton(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (newMessage.value.isBlank() && attachedImages.isEmpty())
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    enabled = newMessage.value.isNotBlank() || attachedImages.isNotEmpty(),
                    onClick = {
                        onSendMessage(newMessage.value, attachedImages)
                        attachedImages = mutableListOf()
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

    if (imageUrlsForScreen.isNotEmpty()) {
        ImagesScreen(
            urls = imageUrlsForScreen,
            initialPage = currentImageIndex,
            titleContent = {
                // todo: from <user>, <time>
            },
            setTopBarState = setTopBarState,
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
private fun ErrorState(
    onRetry: () -> Unit,
) {
    BasicErrorState(onRetry)
}
