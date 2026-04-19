package ru.hse.fandomatch.ui.newpost

import ru.hse.fandomatch.domain.model.Fandom
import ru.hse.fandomatch.domain.model.MediaType

sealed class NewPostState {
    enum class NewPostError {
        CONTENT_TOO_LONG,
        NETWORK,
        IDLE;
    }
    data class Main(
        val content: String = "",
        val attachedFilesWithTypes: List<Pair<ByteArray, MediaType>> = emptyList(),
        val fandoms: List<Fandom> = emptyList(),
        val foundFandoms: List<Fandom> = emptyList(),
        val areFandomsLoading: Boolean = false,
        val isLoading: Boolean = false,
        val contentError: NewPostError = NewPostError.IDLE,
    ) : NewPostState()

    data object Loading : NewPostState()
}

sealed class NewPostEvent {
    data object PostButtonClicked: NewPostEvent()
    data class ContentChanged(val content: String): NewPostEvent()
    data class AttachmentsChanged(val filesWithTypes: List<Pair<ByteArray, MediaType>>): NewPostEvent()
    data class FandomAdded(val fandom: Fandom): NewPostEvent()
    data class FandomRemoved(val fandom: Fandom): NewPostEvent()
    data class FandomSearched(val query: String?): NewPostEvent()
    data object ToastShown: NewPostEvent()
    data object Clear: NewPostEvent()
}

sealed class NewPostAction {
    data object NavigateToPreviousScreen : NewPostAction()
    data object ShowErrorToast : NewPostAction()
}
