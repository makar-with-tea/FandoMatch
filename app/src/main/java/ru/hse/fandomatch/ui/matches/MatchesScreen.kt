package ru.hse.fandomatch.ui.matches

import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.hse.fandomatch.R
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.FandomCategory
import ru.hse.fandomatch.domain.model.Gender
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.ui.composables.LoadingBlock
import ru.hse.fandomatch.ui.composables.SwipeableCardStack

@Composable
fun MatchesScreen(
    navigateToProfile: (String) -> Unit,
    viewModel: MatchesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState()
    val action = viewModel.action.collectAsState()
    Log.i("MatchesScreen", "Rendering MatchesScreen with state: ${state.value}")

        when (val action = action.value) {
            is MatchesAction.NavigateToProfile -> {
                viewModel.obtainEvent(MatchesEvent.Clear)
                navigateToProfile(action.profileId)
            }
            null -> {}
        }

    Log.d("MatchesScreen", "State: $state")

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

            val errorText = state.error.toText()
            if (errorText.isNotEmpty()) {
                Text(errorText)
            }
        }
    }

    if (state.isLoading) {
        LoadingBlock()
    }
}

@Composable
private fun MatchesState.MatchesError.toText() = when (this) {
    MatchesState.MatchesError.NETWORK -> stringResource(id = R.string.network_error)
    MatchesState.MatchesError.NO_PROFILES_FOUND -> stringResource(id = R.string.no_profiles_found_error)
    MatchesState.MatchesError.IDLE -> ""
}

@Composable
private fun LoadingState() = LoadingBlock()

@Composable
private fun IdleState() = LoadingBlock()

@Preview(showBackground = true)
@Composable
fun MainStatePreview() {
    val mockState = MatchesState.Main(
        profileStack = listOf(
            ProfileCard(
                id = "1",
                name = "Alice",
                age = 25,
                avatarUrl = "",
                fandoms = listOf(
                    Fandom(
                        id = "1",
                        name = "One Piece",
                        category = FandomCategory.ANIME_MANGA,
                    )
                ),
                description = "Luffy is my hero!",
                compatibilityPercentage = 85,
                gender = Gender.FEMALE
            )
        ),
        isLoading = false,
        error = MatchesState.MatchesError.IDLE
    )

    MainState(
        state = mockState,
        onLike = { },
        onDislike = { },
        onCardClick = { },
        onReload = { }
    )
}
