package ru.hse.fandomatch.ui.matches

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.exception.LoadDataException
import ru.hse.fandomatch.domain.model.ProfileCard
import ru.hse.fandomatch.domain.usecase.matches.DislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadSuggestedProfilesUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserIdUseCase
import java.util.ArrayDeque
import kotlin.properties.Delegates

class MatchesViewModel(
    private val loadSuggestedProfilesUseCase: LoadSuggestedProfilesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val likeProfileUseCase: LikeProfileUseCase,
    private val dislikeProfileUseCase: DislikeProfileUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {

    private val _state: MutableStateFlow<MatchesState> = MutableStateFlow(MatchesState.Idle)
    val state: StateFlow<MatchesState> get() = _state
    private val _action = MutableStateFlow<MatchesAction?>(null)
    val action: StateFlow<MatchesAction?> get() = _action

    private val buffer: ArrayDeque<ProfileCard> = ArrayDeque()
    private val batchSize: Int = 3
    private val prefetchThreshold: Int = 2
    @Volatile private var isLoadingNext: Boolean = false
    private var userId by Delegates.notNull<Long>()

    fun obtainEvent(event: MatchesEvent) {
        Log.i("MatchesViewModel", "Obtained event: $event")
        when (event) {
            is MatchesEvent.LikedProfile -> {
                likeProfile(event.profileId)
            }
            is MatchesEvent.DislikedProfile -> {
                dislikeProfile(event.profileId)
            }
            is MatchesEvent.LoadSuggestedProfiles -> {
                initialLoad()
            }
            is MatchesEvent.ProfileClicked -> {
                goToProfile(event.profileId)
            }
            is MatchesEvent.Clear -> clear()
        }
    }

    private fun initialLoad() {
        _state.value = MatchesState.Main(
            profileStack = buffer.toList(),
            isLoading = true,
            error = MatchesState.MatchesError.IDLE
        )
        Log.i("MatchesViewModel", "Starting initial load of suggested profiles")
        viewModelScope.launch(dispatcherIO) {
            try {
                userId = getUserIdUseCase.execute() ?: throw LoadDataException()
                val profiles = loadSuggestedProfilesUseCase.execute(userId, batchSize)
                buffer.clear()
                profiles.forEach { buffer.addLast(it) }
                withContext(dispatcherMain) {
                    _state.value = MatchesState.Main(
                        profileStack = buffer.toList(),
                        isLoading = false,
                        error = if (buffer.isEmpty()) MatchesState.MatchesError.NO_PROFILES_FOUND else MatchesState.MatchesError.IDLE
                    )
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = MatchesState.Main(
                        isLoading = false,
                        error = MatchesState.MatchesError.NETWORK
                    )
                }
            }
        }
    }

    private fun likeProfile(profileId: Long) {
        viewModelScope.launch(dispatcherIO) {
            likeProfileUseCase.execute(userId, profileId)
        }
        popAndMaybePrefetch()
    }

    private fun dislikeProfile(profileId: Long) {
        viewModelScope.launch(dispatcherIO) {
            dislikeProfileUseCase.execute(userId, profileId)
        }
        popAndMaybePrefetch()
    }

    private fun goToProfile(profileId: Long) {
        _state.value = MatchesState.Loading
        _action.value = MatchesAction.NavigateToProfile(profileId)
    }

    private fun popAndMaybePrefetch() {
        if (buffer.isNotEmpty()) buffer.removeFirst()

        _state.value = MatchesState.Main(
            profileStack = buffer.toList(),
            isLoading = false,
            error = if (buffer.isEmpty()) MatchesState.MatchesError.NO_PROFILES_FOUND else MatchesState.MatchesError.IDLE
        )

        if (buffer.size <= prefetchThreshold) prefetchNextBatch()
    }

    private fun prefetchNextBatch() {
        if (isLoadingNext) return
        isLoadingNext = true
        viewModelScope.launch(dispatcherIO) {
            try {
                val next = loadSuggestedProfilesUseCase.execute(userId, batchSize)
                withContext(dispatcherMain) {
                    next.forEach { buffer.addLast(it) }
                    _state.value = MatchesState.Main(
                        profileStack = buffer.toList(),
                        isLoading = false,
                        error = MatchesState.MatchesError.IDLE
                    )
                }
            } catch (_: Exception) {
                withContext(dispatcherMain) {
                    _state.value = MatchesState.Main(
                        profileStack = buffer.toList(),
                        isLoading = false,
                        error = if (buffer.isEmpty()) MatchesState.MatchesError.NETWORK else MatchesState.MatchesError.IDLE
                    )
                }
            } finally {
                isLoadingNext = false
            }
        }
    }

    private fun clear() {
        viewModelScope.launch(dispatcherIO) {
            delay(1000) // todo fix??
            _state.value = MatchesState.Idle
            buffer.clear()
            _action.value = null
            isLoadingNext = false
        }
    }
}
