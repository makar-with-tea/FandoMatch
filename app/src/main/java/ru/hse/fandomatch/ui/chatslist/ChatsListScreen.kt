package ru.hse.fandomatch.ui.chatslist

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.MyTitle
import ru.hse.fandomatch.ui.composables.NewMessagesIndicator
import ru.hse.fandomatch.ui.composables.RawImageOrPlaceholder
import ru.hse.fandomatch.ui.composables.SkeletonView
import ru.hse.fandomatch.ui.navigation.EndIconState
import ru.hse.fandomatch.ui.navigation.TopBarState
import ru.hse.fandomatch.ui.utils.timestampToTimeAgo

@Composable
fun ChatsListScreen(
    navigateToChat: (Long) -> Unit,
    setTopBarState: (TopBarState?) -> Unit,
    viewModel: ChatsListViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

    Log.d("ChatsListScreen", "State: $state")
    when (val action = action.value) {
        is ChatsListAction.NavigateToChat -> {
            navigateToChat(action.chatId)
            viewModel.obtainEvent(ChatsListEvent.Clear)
        }

        null -> {}
    }

    when (state.value) {
        is ChatsListState.Main -> {
            MainState(
                state = state.value as ChatsListState.Main,
                onChatClicked = { id -> viewModel.obtainEvent(ChatsListEvent.ChatClicked(id)) },
                setTopBarState = setTopBarState,
                onSearch = { query -> viewModel.obtainEvent(ChatsListEvent.SearchChats(query)) }
            )
        }
        is ChatsListState.Idle -> {
            IdleState()
            viewModel.obtainEvent(ChatsListEvent.LoadChats)
        }
        is ChatsListState.Loading -> {
            LoadingState()
        }
    }
}

@Composable
private fun MainState(
    state: ChatsListState.Main,
    onChatClicked: (Long) -> Unit,
    setTopBarState: (TopBarState?) -> Unit,
    onSearch: (String?) -> Unit,
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }

    setTopBarState(
        TopBarState(
            titleContent = {
                if (isSearchActive) {
                    TextField(
                        value = searchInput,
                        onValueChange = { input -> searchInput = input },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { onSearch(searchInput) }
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_name_input),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        trailingIcon = {
                            if (searchInput.isNotBlank())
                                IconButton(
                                    onClick = { searchInput = "" }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                                        contentDescription = stringResource(R.string.clear_search_icon_description),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                } else MyTitle(text = stringResource(R.string.chats_list_title))
            },
            endIcons = if (isSearchActive) listOf() else listOf(
                EndIconState(
                    iconId = R.drawable.ic_search,
                    onClick = {
                        isSearchActive = true
                    },
                    descriptionId = R.string.search_icon_description
                ),
            ),
        )
    )

    BackHandler(enabled = isSearchActive) {
        onSearch(null)
        searchInput = ""
        isSearchActive = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(state.chats) { chat ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(vertical = 1.dp, horizontal = 4.dp)
                    .clickable { onChatClicked(chat.chatId) }
            ) {
                Row {
                    RawImageOrPlaceholder(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(top = 2.dp, bottom = 2.dp, end = 2.dp)
                            .clip(CircleShape),
                        url = chat.participantAvatarUrl,
                        placeholderId = R.drawable.ic_account_placeholder,
                        context = LocalContext.current,
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                text = chat.participantName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = timestampToTimeAgo(
                                    timestamp = chat.lastMessageTimestamp,
                                    context = LocalContext.current
                                ),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                            )
                        }

                        Text(
                            text = chat.lastMessage,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    NewMessagesIndicator(
                        count = chat.newMessagesCount,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

@Composable
private fun LoadingState() {
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
    val screenHeight = with(LocalDensity.current) {
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
    ) {
        repeat(5) {
            // todo avatars separately from text blocks?
            SkeletonView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                globalX = globalX,
                globalY = globalY,
                endX = endX,
                endY = endY,
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }
    }
}

@Composable
private fun IdleState() = LoadingState()

@Preview(showBackground = true)
@Composable
private fun ChatsListScreenPreview() {
    ChatsListScreen({}, {})
}
