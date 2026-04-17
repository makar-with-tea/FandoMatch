package ru.hse.fandomatch.ui.newpost

sealed class NewPostState {
    enum class NewPostError {
        CONTENT_TOO_LONG,
        NETWORK,
        IDLE;
    }
    data class Main(
        val content: String = "",
        val attachedImages: List<ByteArray> = emptyList(),
        val fandoms: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val contentError: NewPostError = NewPostError.IDLE,
    ) : NewPostState()

    data object Loading : NewPostState()
}

sealed class NewPostEvent {
    data object PostButtonClicked: NewPostEvent()
    data class ContentChanged(val content: String): NewPostEvent()
    data class AttachmentsChanged(val images: List<ByteArray>): NewPostEvent()
    data object Clear: NewPostEvent()
}

sealed class NewPostAction {
    data object NavigateToPreviousScreen : NewPostAction()
}
