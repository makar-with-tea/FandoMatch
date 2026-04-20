package ru.hse.fandomatch.ui.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.model.Comment
import ru.hse.fandomatch.domain.model.MediaItem
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.usecase.media.DownloadMediaToGalleryUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetFullPostUseCase
import ru.hse.fandomatch.domain.usecase.posts.LikePostUseCase
import ru.hse.fandomatch.domain.usecase.posts.SendCommentUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase
import java.time.Instant

class PostViewModel(
    private val getFullPostUseCase: GetFullPostUseCase,
    private val sendCommentUseCase: SendCommentUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val downloadMediaToGalleryUseCase: DownloadMediaToGalleryUseCase,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<PostState> = MutableStateFlow(PostState.Idle)
    val state: StateFlow<PostState> get() = _state
    private val _action = MutableStateFlow<PostAction?>(null)
    val action: StateFlow<PostAction?> get() = _action

    fun obtainEvent(event: PostEvent) {
        Log.i("PostViewModel", "Obtained event: $event")
        when (event) {
            PostEvent.Clear -> clear()
            is PostEvent.LoadPost -> loadPost(event.postId)
            PostEvent.ProfileClicked -> goToProfile()
            PostEvent.LikeClicked -> onLikeClicked()
            is PostEvent.DownloadMediaItem -> downloadMediaItem(event.mediaItem)
            PostEvent.ToastShown -> toastShown()
            is PostEvent.UpdateCommentDraft -> updateCommentDraft(event.commentDraft)
            PostEvent.SendComment -> sendComment()
        }
    }

    private fun loadPost(profileId: String?) {
        _state.value = PostState.Loading
        if (profileId == null) {
            _state.value = PostState.Error
            return
        }
        viewModelScope.launch(dispatcherIO) {
            val result = getFullPostUseCase.execute(postId = profileId)
            val fullPost = result.getOrNull() ?: run {
                Log.e("PostViewModel", "Failed to load post info", result.exceptionOrNull())
                _state.value = PostState.Error
                return@launch
            }
            _state.value = PostState.Main(fullPost)
        }
    }

    private fun sendComment() {
        val timestamp = Instant.now().toEpochMilli()
        val currentState = (_state.value as? PostState.Main) ?: return
        val commentText = currentState.commentDraft.trim()
        if (commentText.isEmpty()) return
        viewModelScope.launch(dispatcherIO) {
            val result = sendCommentUseCase.execute(
                postId = currentState.fullPost.post.id,
                content = commentText,
                timestamp = timestamp,
            )
            if (result.isFailure) {
                Log.e("PostViewModel", "Failed to send comment", result.exceptionOrNull())
                return@launch
            }
            val currentUser = getUserUseCase.execute(null, true).getOrNull() ?: run {
                Log.e("PostViewModel", "Failed to load current user info", result.exceptionOrNull())
                return@launch
            }
            withContext(dispatcherMain) {
                _state.value = currentState.copy(
                    commentDraft = "",
                    fullPost = currentState.fullPost.copy(
                        comments = currentState.fullPost.comments + Comment(
                            authorName = currentUser.name,
                            authorLogin = (currentUser.profileType as ProfileType.Own).login,
                            authorAvatar = currentUser.avatar,
                            content = commentText,
                            timestamp = timestamp,
                        )
                    )
                )
            }
        }
    }

    private fun goToProfile() {
        when (val currentState = _state.value) {
            is PostState.Main -> _action.value = PostAction.GoToProfile(
                profileId = currentState.fullPost.post.authorId
            )

            else -> Unit
        }
    }

    private fun onLikeClicked() {
        val currentState = (_state.value as? PostState.Main) ?: return
        viewModelScope.launch(dispatcherIO) {
            val result = likePostUseCase.execute(postId = currentState.fullPost.post.id)
            if (result.isFailure) {
                Log.e("PostViewModel", "Failed to like post", result.exceptionOrNull())
                return@launch
            }
            _state.value = currentState.copy(
                fullPost = currentState.fullPost.copy(
                    post = currentState.fullPost.post.copy(
                        isLikedByCurrentUser = !currentState.fullPost.post.isLikedByCurrentUser,
                        likeCount = if (currentState.fullPost.post.isLikedByCurrentUser) {
                            currentState.fullPost.post.likeCount - 1
                        } else {
                            currentState.fullPost.post.likeCount + 1
                        }
                    )
                )
            )
        }
    }

    private fun updateCommentDraft(commentDraft: String) {
        val currentState = (_state.value as? PostState.Main) ?: return
        _state.value = currentState.copy(commentDraft = commentDraft)
    }

    private fun downloadMediaItem(mediaItem: MediaItem) {
        viewModelScope.launch(dispatcherIO) {
            downloadMediaToGalleryUseCase.execute(
                mediaUrl = mediaItem.url,
                mediaType = mediaItem.mediaType
            )
                .onFailure {
                    Log.e("PostViewModel", "Failed to download media item", it)
                    _action.value = PostAction.ShowErrorDownloadToast
                }
                .onSuccess {
                    _action.value = PostAction.ShowSuccessDownloadToast
                }
        }
    }

    private fun toastShown() {
        _action.value = null
    }

    private fun clear() {
        _state.value = PostState.Idle
        _action.value = null
    }
}
