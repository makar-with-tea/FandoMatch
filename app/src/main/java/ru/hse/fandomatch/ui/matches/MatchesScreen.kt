package ru.hse.fandomatch.ui.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.ui.composables.BasicErrorState
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.SwipeableCardStack

@Composable
fun MatchesScreen(
    navigateToProfile: (String) -> Unit,
    viewModel: MatchesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()

        when (val action = action.value) {
            is MatchesAction.NavigateToProfile -> {
                viewModel.obtainEvent(MatchesEvent.Clear)
                navigateToProfile(action.profileId)
            }
            null -> {}
        }

    when (state.value) {
        is MatchesState.Idle -> {
            viewModel.obtainEvent(MatchesEvent.LoadSuggestedProfiles)
            IdleState()
        }
        is MatchesState.Main -> MainState(
            state = state.value as MatchesState.Main,
            onLike = { id -> viewModel.obtainEvent(MatchesEvent.LikedProfile(id)) },
            onDislike = { id -> viewModel.obtainEvent(MatchesEvent.DislikedProfile(id)) },
            onCardClick = { id -> viewModel.obtainEvent(MatchesEvent.ProfileClicked(id)) },
            onReload = { viewModel.obtainEvent(MatchesEvent.LoadSuggestedProfiles) }
        )
        is MatchesState.Loading -> LoadingState()
        is MatchesState.Error -> ErrorState(
            onRetry = { viewModel.obtainEvent(MatchesEvent.LoadSuggestedProfiles) }
        )
    }
}

@Composable
private fun MainState(
    state: MatchesState.Main,
    onLike: (String) -> Unit,
    onDislike: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onReload: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {

            if (state.profileStack.isNotEmpty()) {
                SwipeableCardStack(
                    profiles = state.profileStack.reversed(),
                    onLike = onLike,
                    onDislike = onDislike,
                    onCardClick = onCardClick,
                )
            } else {
                Text(stringResource(id = R.string.no_profiles_found_error))
                OutlinedButton(onClick = onReload) {
                    Text(stringResource(id = R.string.retry_load))
                }
            }
        }
    }

    if (state.isLoading) {
        LoadingBlock()
    }
}

@Composable
private fun LoadingState() = LoadingBlock()

@Composable
private fun IdleState() = LoadingBlock()

@Composable
private fun ErrorState(
    onRetry: () -> Unit
) {
    BasicErrorState { onRetry() }
}
