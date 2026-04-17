package ru.hse.fandomatch.ui.post

import ru.hse.fandomatch.domain.model.FullPost

sealed class PostState {
    enum class PostError {
        IDLE,
        NETWORK,
    }
    data class Main(
        val fullPost: FullPost,
        val commentDraft: String = "",
        val images: List<String> = emptyList(),
        val imagesOpen: Boolean = false,
    ) : PostState()

    data object Loading : PostState()
    data object Error : PostState()
    data object Idle : PostState()
}

sealed class PostEvent {
    data class LoadPost(val postId: String?) : PostEvent()
    data class UpdateCommentDraft(val commentDraft: String) : PostEvent()
    data object SendComment : PostEvent()
    data object ProfileClicked : PostEvent()
    data object LikeClicked : PostEvent()
    data object ImagesClicked : PostEvent()
    data object ImagesClosed : PostEvent()
    data object Clear : PostEvent()
}

sealed class PostAction {
    data class GoToProfile(val profileId: String) : PostAction()
}
