package ru.hse.fandomatch.ui.newpost

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.utils.MAX_NUMBER_OF_ATTACHMENTS
import ru.hse.fandomatch.utils.checkPostContentLength
import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.MediaType
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.fandoms.GetFandomsByQueryUseCase
import ru.hse.fandomatch.domain.usecase.posts.CreatePostUseCase

class NewPostViewModel(
    private val createPostUseCase: CreatePostUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val getFandomsByQueryUseCase: GetFandomsByQueryUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<NewPostState> =
        MutableStateFlow(NewPostState.Main())
    val state: StateFlow<NewPostState>
        get() = _state
    private val _action = MutableStateFlow<NewPostAction?>(null)
    val action: StateFlow<NewPostAction?>
        get() = _action

    fun obtainEvent(event: NewPostEvent) {
        Log.d("NewPostViewModel", "Event: $event")
        when (event) {
            is NewPostEvent.AttachmentsChanged -> attachmentsChanged(event.filesWithTypes)
            is NewPostEvent.ContentChanged -> contentChanged(event.content)
            NewPostEvent.PostButtonClicked -> post()
            is NewPostEvent.FandomAdded -> addFandom(event.fandom)
            is NewPostEvent.FandomRemoved -> removeFandom(event.fandom)
            is NewPostEvent.FandomSearched -> searchFandom(event.query)
            NewPostEvent.ToastShown -> _action.value = null
            is NewPostEvent.Clear -> clear()
        }
    }

    private fun contentChanged(content: String) {
        val currentState = _state.value as? NewPostState.Main ?: return
        val contentError = if (!content.checkPostContentLength())
            NewPostState.NewPostError.CONTENT_TOO_LONG else NewPostState.NewPostError.IDLE
        _state.value = currentState.copy(
            content = content,
            contentError = contentError,
        )
    }

    private fun attachmentsChanged(filesWithTypes: List<Pair<ByteArray, MediaType>>) {
        val currentState = _state.value as? NewPostState.Main ?: return
        _state.value = currentState.copy(
            attachedFilesWithTypes = filesWithTypes.take(MAX_NUMBER_OF_ATTACHMENTS)
        )
    }

    private fun addFandom(fandom: Fandom) {
        val currentState = state.value
        if (currentState is NewPostState.Main && fandom !in currentState.fandoms) {
            _state.value = currentState.copy(fandoms = currentState.fandoms + fandom)
        }
    }

    private fun removeFandom(fandom: Fandom) {
        val currentState = state.value
        if (currentState is NewPostState.Main) {
            _state.value = currentState.copy(fandoms = currentState.fandoms - fandom)
        }
    }

    private fun searchFandom(query: String?) {
        val currentState = state.value as? NewPostState.Main ?: return
        if (query.isNullOrBlank()) {
            _state.value = currentState.copy(foundFandoms = emptyList())
            return
        }
        _state.value = currentState.copy(foundFandoms = emptyList(), areFandomsLoading = true)
        viewModelScope.launch(dispatcherIO) {
            val result = getFandomsByQueryUseCase.execute(query)
            val foundFandoms = result.getOrNull() ?: run {
                Log.e(
                    "NewPostViewModel",
                    "Failed to search fandoms: ${result.exceptionOrNull()}"
                )
                withContext(dispatcherMain) {
                    _state.value =
                        currentState.copy(foundFandoms = emptyList(), areFandomsLoading = false)
                }
                return@launch
            }
            withContext(dispatcherMain) {
                _state.value =
                    currentState.copy(foundFandoms = foundFandoms, areFandomsLoading = false)
            }
        }
    }

    private fun post() {
        val currentState = _state.value as? NewPostState.Main ?: return
        viewModelScope.launch(dispatcherIO) {
            val mediaIdsWithTypes = currentState.attachedFilesWithTypes.mapNotNull { (bytes, type) ->
                val uploadResult = uploadMediaUseCase.execute(bytes, type)
                val mediaId = uploadResult.getOrNull()
                mediaId ?: run {
                    Log.e("NewPostViewModel", "Failed to upload media: ${uploadResult.exceptionOrNull()}")
                    return@mapNotNull null
                }
                mediaId to type
            }
            val result = createPostUseCase.execute(
                content = currentState.content,
                mediaIdsWithTypes = mediaIdsWithTypes,
                fandomIds = currentState.fandoms.map { it.id },
            )
            if (result.isFailure) {
                Log.e("NewPostViewModel", "Failed to create post: ${result.exceptionOrNull()}")
                _action.value = NewPostAction.ShowErrorToast // todo надо ли его потом очищать?
                return@launch
            }
            withContext(dispatcherMain) {
                _action.value = NewPostAction.NavigateToPreviousScreen
            }
        }
    }

    private fun clear() {
        _state.value = NewPostState.Main()
        _action.value = null
    }
}
