package ru.hse.fandomatch.ui.newpost

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.MAX_NUMBER_OF_ATTACHMENTS
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.getBytesFromUri
import ru.hse.fandomatch.navigation.TopBarState
import ru.hse.fandomatch.ui.composables.AttachmentsRow
import ru.hse.fandomatch.ui.composables.FandomInput
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.MyTextField
import ru.hse.fandomatch.ui.composables.MyTitle
import kotlin.collections.plus

@Composable
fun NewPostScreen(
    navigateToPreviousScreen: () -> Unit,
    setTopBarState: (TopBarState) -> Unit,
    viewModel: NewPostViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("NewPostScreen", "State: $state")
    when (action.value) {
        is NewPostAction.NavigateToPreviousScreen -> {
            navigateToPreviousScreen()
            viewModel.obtainEvent(NewPostEvent.Clear)
        }

        is NewPostAction.ShowErrorToast -> {
            Toast.makeText(LocalContext.current, R.string.network_error, Toast.LENGTH_SHORT).show()
            viewModel.obtainEvent(NewPostEvent.ToastShown)
        }

        null -> {}
    }

    when (state.value) {
        is NewPostState.Main -> {
            MainState(
                state.value as NewPostState.Main,
                onContentChanged = { viewModel.obtainEvent(NewPostEvent.ContentChanged(it)) },
                onAttachmentsChanged = { viewModel.obtainEvent(NewPostEvent.AttachmentsChanged(it)) },
                onPostClick = { viewModel.obtainEvent(NewPostEvent.PostButtonClicked) },
                onFandomAdded = { viewModel.obtainEvent(NewPostEvent.FandomAdded(it)) },
                onFandomRemoved = { viewModel.obtainEvent(NewPostEvent.FandomRemoved(it)) },
                onFandomSearched = { viewModel.obtainEvent(NewPostEvent.FandomSearched(it)) },
                setTopBarState = setTopBarState,
            )
        }
        is NewPostState.Loading -> {
            LoadingState()
        }
    }
}

@Composable
fun MainState(
    state: NewPostState.Main,
    onContentChanged: (String) -> Unit,
    onAttachmentsChanged: (List<Pair<ByteArray, MediaType>>) -> Unit,
    onPostClick: () -> Unit,
    onFandomAdded: (Fandom) -> Unit,
    onFandomRemoved: (Fandom) -> Unit,
    onFandomSearched: (String?) -> Unit,
    setTopBarState: (TopBarState) -> Unit,
) {
    setTopBarState(
        TopBarState(
            titleContent = @Composable {
                Row {
                    MyTitle(
                        text = stringResource(R.string.new_post_title),
                    )
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.post_button),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.onSecondaryContainer)
                            .clickable { onPostClick() }
                            .padding(8.dp)
                    )
                }
            },
            endIcons = listOf()
        )
    )

    var content by remember { mutableStateOf(state.content) }
    var attachedFiles by remember { mutableStateOf(state.attachedFilesWithTypes) }

    val context = LocalContext.current
    val pickMedia = when (MAX_NUMBER_OF_ATTACHMENTS - attachedFiles.size) {
        0 -> null

        1 -> rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            val type = context.contentResolver.getType(uri ?: return@rememberLauncherForActivityResult)
                ?: return@rememberLauncherForActivityResult
            val mediaType = when {
                type.startsWith("image") -> MediaType.IMAGE
                type.startsWith("video") -> MediaType.VIDEO
                else -> return@rememberLauncherForActivityResult
            }
            uri.let {
                getBytesFromUri(context, it)?.let { byteArray ->
                    attachedFiles = attachedFiles + (byteArray to mediaType)
                    onAttachmentsChanged(attachedFiles)
                }
            }
        }

        else -> rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(
                maxItems = maxOf(MAX_NUMBER_OF_ATTACHMENTS - attachedFiles.size)
            )
        ) { uris ->
            attachedFiles = attachedFiles + uris.mapNotNull {
                val type = context.contentResolver.getType(it) ?: return@mapNotNull null
                val mediaType = when {
                    type.startsWith("image") -> MediaType.IMAGE
                    type.startsWith("video") -> MediaType.VIDEO
                    else -> return@mapNotNull null
                }
                val bytes = getBytesFromUri(context, it) ?: return@mapNotNull null
                bytes to mediaType
            }
            onAttachmentsChanged(
                attachedFiles
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            MyTextField(
                modifier = Modifier
                    .fillMaxSize(),
                value = content,
                label = null,
                placeholder = stringResource(id = R.string.post_content_label),
                isError = state.contentError != NewPostState.NewPostError.IDLE,
                errorText = state.contentError.toText(),
                hideOnDone = false
            ) {
                onContentChanged(it)
                content = it
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 32.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                enabled = attachedFiles.size < MAX_NUMBER_OF_ATTACHMENTS,
                onClick = {
                    pickMedia?.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageAndVideo
                        )
                    )
                },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_attach),
                    contentDescription = stringResource(id = R.string.attach_file_button_description),
                )
            }
        }

        AttachmentsRow(
            attachedFilesWithTypes = attachedFiles,
            onAttachmentsChanged = {
                onAttachmentsChanged(it)
                attachedFiles = it
            },
        )

        Text(
            text = stringResource(id = R.string.post_fandoms_label),
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        FandomInput(
            foundFandoms = state.foundFandoms,
            selectedFandoms = state.fandoms,
            onFandomAdded = onFandomAdded,
            onFandomRemoved = onFandomRemoved,
            onSearch = onFandomSearched,
            areFandomsLoading = state.areFandomsLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    if (state.isLoading) {
        LoadingBlock()
    }
}

@Composable
private fun NewPostState.NewPostError.toText() = when (this) {
    NewPostState.NewPostError.CONTENT_TOO_LONG ->
        stringResource(id = R.string.post_content_too_long_error)
    NewPostState.NewPostError.NETWORK ->
        stringResource(id = R.string.network_error)
    NewPostState.NewPostError.IDLE -> ""
}

@Composable
fun LoadingState() {
    LoadingBlock()
}

@Preview(showBackground = true)
@Composable
fun NewPostScreenPreview() {
    NewPostScreen(
            {}, {}
    )
}
