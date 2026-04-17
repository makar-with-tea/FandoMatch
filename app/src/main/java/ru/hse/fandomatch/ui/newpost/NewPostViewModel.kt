package ru.hse.fandomatch.ui.newpost

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.hse.fandomatch.MAX_NUMBER_OF_ATTACHMENTS
import ru.hse.fandomatch.checkPostContentLength

class NewPostViewModel(
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
            is NewPostEvent.AttachmentsChanged -> attachmentsChanged(event.images)
            is NewPostEvent.ContentChanged -> contentChanged(event.content)
            NewPostEvent.PostButtonClicked -> post()
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

    private fun attachmentsChanged(images: List<ByteArray>) {
        val currentState = _state.value as? NewPostState.Main ?: return
        _state.value = currentState.copy(
            attachedImages = images.take(MAX_NUMBER_OF_ATTACHMENTS)
        )
    }

    private fun post() {
        // todo backend
        _action.value = NewPostAction.NavigateToPreviousScreen
    }

    private fun clear() {
        _state.value = NewPostState.Main()
        _action.value = null
    }
}
