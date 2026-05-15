package ru.hse.fandomatch.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.fandomatch.domain.logging.Logger
import ru.hse.fandomatch.domain.model.Post
import ru.hse.fandomatch.domain.model.ProfileType
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetUserPostsUseCase
import ru.hse.fandomatch.domain.usecase.posts.LikePostUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendRequestsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase

class ProfileViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val likeOrDislikeProfileUseCase: LikeOrDislikeProfileUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val getFriendRequestsUseCase: GetFriendRequestsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val logger: Logger,
    val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
): ViewModel() {
    private val _state: MutableStateFlow<ProfileState> = MutableStateFlow(ProfileState.Idle)
    val state: StateFlow<ProfileState> get() = _state
    private val _action = MutableStateFlow<ProfileAction?>(null)
    val action: StateFlow<ProfileAction?> get() = _action

    private var currentPosts: List<Post> = emptyList()
    private var hasMorePosts: Boolean = true
    private var isLoadingMorePosts: Boolean = false

    private companion object {
        const val PROFILE_POSTS_PAGE_SIZE = 30
    }

    fun obtainEvent(event: ProfileEvent) {
        logger.i("ProfileViewModel", "Obtained event: $event")
        when (event) {
            is ProfileEvent.LoadProfile -> {
                loadProfile(event.userId, event.isCurrentUser)
            }
            is ProfileEvent.MessageButtonClicked -> {
                _action.value = ProfileAction.GoToMessages(event.userId)
            }
            ProfileEvent.EditProfileButtonClicked -> {
                _action.value = ProfileAction.GoToEditProfile
            }
            ProfileEvent.SettingsButtonClicked -> {
                _action.value = ProfileAction.GoToSettings
            }
            ProfileEvent.AddPostButtonClicked -> {
                _action.value = ProfileAction.GoToAddPost
            }
            is ProfileEvent.LikeButtonClicked -> {
                likeOrDislikeProfile(event.profileId, isLike = true)
            }
            is ProfileEvent.DislikeButtonClicked -> {
                likeOrDislikeProfile(event.profileId, isLike = false)
            }
            ProfileEvent.FriendsButtonClicked -> loadFriends()
            ProfileEvent.PostsButtonClicked -> loadPosts()
            ProfileEvent.RequestsButtonClicked -> loadRequests()
            is ProfileEvent.ProfileClicked -> {
                _action.value = ProfileAction.GoToProfile(event.profileId)
            }
            is ProfileEvent.PostClicked -> {
                _action.value = ProfileAction.GoToPost(event.postId)
            }
            is ProfileEvent.PostLiked -> likePost(event.postId)
            ProfileEvent.LoadMorePosts -> loadMorePosts()
            ProfileEvent.Clear -> clear()
        }
    }

    private fun loadProfile(userId: String?, isCurrentUser: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            getUserUseCase.execute(userId, isCurrentUser)
                .onFailure { exception ->
                    logger.e("ProfileViewModel", "Failed to load profile info", exception)
                    withContext(dispatcherMain) {
                        _state.value = ProfileState.Error
                    }
                    return@launch
                }
                .onSuccess { user ->
                    var postsIsError = false
                    var posts: List<Post>? = null
                    getUserPostsUseCase.execute(
                        profileId = user.id,
                        isCurrentUser = isCurrentUser,
                        beforeTimestamp = null,
                        size = PROFILE_POSTS_PAGE_SIZE,
                    )
                        .onFailure { exception ->
                            logger.e("ProfileViewModel", "Failed to load profile posts", exception)
                            postsIsError = true
                        }
                        .onSuccess {
                            posts = it
                            currentPosts = it
                            hasMorePosts = it.size == PROFILE_POSTS_PAGE_SIZE
                            isLoadingMorePosts = false
                        }
                    val type = user.profileType
                    logger.i("ProfileViewModel", "Loading profile for userId: $userId")
                    withContext(dispatcherMain) {
                        _state.value = ProfileState.Main(
                            id = user.id,
                            login = when (type) {
                                is ProfileType.Stranger -> null
                                is ProfileType.Own -> type.login
                                is ProfileType.Friend -> type.login
                            },
                            fandoms = user.fandoms,
                            description = user.description,
                            name = user.name,
                            gender = user.gender,
                            age = user.age,
                            avatarUrl = user.avatar?.url,
                            backgroundUrl = user.background?.url,
                            city = user.city,
                            type = type,
                            bottomSheetState = ProfileState.BottomSheetState.Posts(
                                posts = posts ?: emptyList(),
                                isError = postsIsError,
                                hasMore = hasMorePosts,
                                isLoadingMore = false,
                            ),
                        )
                    }
                }
        }
    }

    private fun likeOrDislikeProfile(profileId: String, isLike: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            val result = likeOrDislikeProfileUseCase.execute(profileId, isLike)
            if (result.isFailure) {
                logger.e("ProfileViewModel", "Failed to ${if (isLike) "like" else "dislike"} profile $profileId", result.exceptionOrNull())
            }
            withContext(dispatcherMain) {
                _action.value = ProfileAction.GoToMatches
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch(dispatcherIO) {
            val currentState = (_state.value as? ProfileState.Main) ?: return@launch
            val userId = currentState.id
            val isCurrentUser = currentState.type is ProfileType.Own
            getUserPostsUseCase.execute(
                profileId = userId,
                isCurrentUser = isCurrentUser,
                beforeTimestamp = null,
                size = PROFILE_POSTS_PAGE_SIZE,
            )
                .onFailure {
                    logger.e("ProfileViewModel", "Failed to load profile posts", it)
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            bottomSheetState = ProfileState.BottomSheetState.Posts(
                                posts = emptyList(),
                                isError = true,
                                hasMore = false,
                                isLoadingMore = false,
                            )
                        )
                    }
                }
                .onSuccess { posts ->
                    currentPosts = posts
                    hasMorePosts = posts.size == PROFILE_POSTS_PAGE_SIZE
                    isLoadingMorePosts = false
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            bottomSheetState = ProfileState.BottomSheetState.Posts(
                                posts = posts,
                                isError = false,
                                hasMore = hasMorePosts,
                                isLoadingMore = false,
                            )
                        )
                    }
                }
        }
    }

    private fun loadMorePosts() {
        if (isLoadingMorePosts || !hasMorePosts) return
        val currentState = _state.value as? ProfileState.Main ?: return
        val postsState =
            currentState.bottomSheetState as? ProfileState.BottomSheetState.Posts ?: return
        val oldestTimestamp = currentPosts.minOfOrNull { it.timestamp } ?: return

        isLoadingMorePosts = true
        _state.value = currentState.copy(
            bottomSheetState = postsState.copy(isLoadingMore = true)
        )

        viewModelScope.launch(dispatcherIO) {
            getUserPostsUseCase.execute(
                profileId = currentState.id,
                isCurrentUser = currentState.type is ProfileType.Own,
                beforeTimestamp = oldestTimestamp,
                size = PROFILE_POSTS_PAGE_SIZE,
            )
                .onFailure { exception ->
                    logger.e("ProfileViewModel", "Failed to load more profile posts", exception)
                    isLoadingMorePosts = false
                    withContext(dispatcherMain) {
                        val latestState = _state.value as? ProfileState.Main ?: return@withContext
                        val latestPostsState =
                            latestState.bottomSheetState as? ProfileState.BottomSheetState.Posts
                                ?: return@withContext
                        _state.value = latestState.copy(
                            bottomSheetState = latestPostsState.copy(isLoadingMore = false)
                        )
                    }
                    return@launch
                }
                .onSuccess { olderPosts ->
                    currentPosts = (currentPosts + olderPosts)
                        .distinctBy { it.id }
                        .sortedByDescending { it.timestamp }
                    hasMorePosts = olderPosts.size == PROFILE_POSTS_PAGE_SIZE
                    isLoadingMorePosts = false
                    withContext(dispatcherMain) {
                        val latestState = _state.value as? ProfileState.Main ?: return@withContext
                        val latestPostsState =
                            latestState.bottomSheetState as? ProfileState.BottomSheetState.Posts
                                ?: return@withContext
                        _state.value = latestState.copy(
                            bottomSheetState = latestPostsState.copy(
                                posts = currentPosts,
                                isError = false,
                                hasMore = hasMorePosts,
                                isLoadingMore = false,
                            )
                        )
                    }
                }
        }
    }

    private fun loadFriends() {
        val currentState = _state.value
        if (currentState !is ProfileState.Main) return
        viewModelScope.launch(dispatcherIO) {
            getFriendsUseCase.execute()
                .onFailure { exception ->
                    logger.e("ProfileViewModel", "Failed to load friends", exception)
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            bottomSheetState = ProfileState.BottomSheetState.Friends(
                                emptyList(),
                                true
                            )
                        )
                    }
                }
                .onSuccess { friends ->
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            bottomSheetState = ProfileState.BottomSheetState.Friends(friends, false)
                        )
                    }
                }
        }
    }

    private fun loadRequests() {
        val currentState = _state.value
        if (currentState !is ProfileState.Main) return
        viewModelScope.launch(dispatcherIO) {
            getFriendRequestsUseCase.execute()
                .onFailure { exception ->
                    logger.e("ProfileViewModel", "Failed to load friend requests", exception)
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            bottomSheetState = ProfileState.BottomSheetState.Requests(
                                emptyList(),
                                true
                            )
                        )
                    }
                }
                .onSuccess { requests ->
                    withContext(dispatcherMain) {
                        _state.value = currentState.copy(
                            bottomSheetState = ProfileState.BottomSheetState.Requests(
                                requests,
                                false
                            )
                        )
                    }
                }
        }
    }

    private fun likePost(postId: String) {
        viewModelScope.launch(dispatcherIO) {
            likePostUseCase.execute(postId)
                .onFailure { exception ->
                    logger.e("ProfileViewModel", "Failed to like post", exception)
                }
                .onSuccess {
                    withContext(dispatcherMain) {
                        val currentState = _state.value as? ProfileState.Main ?: return@withContext
                        val postsState = currentState.bottomSheetState as? ProfileState.BottomSheetState.Posts ?: return@withContext
                        val updatedPosts = postsState.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(
                                    likeCount = if (post.isLikedByCurrentUser) post.likeCount - 1 else post.likeCount + 1,
                                    isLikedByCurrentUser = !post.isLikedByCurrentUser,
                                )
                            } else {
                                post
                            }
                        }
                        _state.value = currentState.copy(
                            bottomSheetState = postsState.copy(
                                posts = updatedPosts,
                                isError = false,
                            )
                        )
                    }
                }
        }
    }

    private fun clear() {
        _state.value = ProfileState.Idle
        _action.value = null
    }
}
