package ru.hse.fandomatch.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import ru.hse.fandomatch.data.model.CommentListResponseDTO
import ru.hse.fandomatch.data.model.CommentsGetRequestDTO
import ru.hse.fandomatch.data.model.CreatePostRequestDTO
import ru.hse.fandomatch.data.model.CreatePostResponseDTO
import ru.hse.fandomatch.data.model.CurrentFiltersResponseDTO
import ru.hse.fandomatch.data.model.EditUserProfileRequestDTO
import ru.hse.fandomatch.data.model.EditUserProfileResponseDTO
import ru.hse.fandomatch.data.model.ExtendedPostResponseDTO
import ru.hse.fandomatch.data.model.FandomCategoryListResponseDTO
import ru.hse.fandomatch.data.model.FandomListResponseDTO
import ru.hse.fandomatch.data.model.FandomRequestCreateDTO
import ru.hse.fandomatch.data.model.FandomRequestCreateResponseDTO
import ru.hse.fandomatch.data.model.FandomsGetRequestDTO
import ru.hse.fandomatch.data.model.FriendsResponseDTO
import ru.hse.fandomatch.data.model.GetFeedRequestDTO
import ru.hse.fandomatch.data.model.GetRelatedUsersRequestDTO
import ru.hse.fandomatch.data.model.MatchActionRequestDTO
import ru.hse.fandomatch.data.model.MatchActionResponseDTO
import ru.hse.fandomatch.data.model.MatchBatchRequestDTO
import ru.hse.fandomatch.data.model.MatchCandidateBatchResponseDTO
import ru.hse.fandomatch.data.model.MatchFilterRequestDTO
import ru.hse.fandomatch.data.model.MatchFilterResponseDTO
import ru.hse.fandomatch.data.model.PendingRequestsResponseDTO
import ru.hse.fandomatch.data.model.PostLikeResponseDTO
import ru.hse.fandomatch.data.model.PostListResponseDTO
import ru.hse.fandomatch.data.model.PostsGetRequestDTO
import ru.hse.fandomatch.data.model.UserProfileRequestDTO
import ru.hse.fandomatch.data.model.UserProfileResponseDTO

interface CoreApiService {

    // USER
    @POST("core/user/profile")
    suspend fun getUserProfile(
        @Body request: UserProfileRequestDTO
    ): UserProfileResponseDTO

    @PATCH("core/user/profile/edit")
    suspend fun editUserProfile(
        @Body request: EditUserProfileRequestDTO
    ): EditUserProfileResponseDTO

    @PATCH("core/user/profile/friends")
    suspend fun getFriends(
        @Body request: GetRelatedUsersRequestDTO
    ): FriendsResponseDTO

    @PATCH("core/user/profile/pending_requests")
    suspend fun getPendingRequests(
        @Body request: GetRelatedUsersRequestDTO
    ): PendingRequestsResponseDTO

    // MATCHING
    @POST("core/match/next")
    suspend fun getMatchCandidates(
        @Body request: MatchBatchRequestDTO
    ): MatchCandidateBatchResponseDTO

    @POST("core/match/react")
    suspend fun reactOnMatch(
        @Body request: MatchActionRequestDTO
    ): MatchActionResponseDTO

    @POST("core/match/filter")
    suspend fun setMatchFilter(
        @Body request: MatchFilterRequestDTO
    ): MatchFilterResponseDTO

    @GET("core/match/get_current_filters")
    suspend fun getCurrentFilters(): CurrentFiltersResponseDTO

    // POSTS
    @POST("core/posts/get")
    suspend fun getPosts(
        @Body request: PostsGetRequestDTO
    ): PostListResponseDTO

    @POST("core/posts/create")
    suspend fun createPost(
        @Body request: CreatePostRequestDTO
    ): CreatePostResponseDTO

    @GET("core/posts/{post_id}")
    suspend fun getPost(
        @Path("post_id") postId: String
    ): ExtendedPostResponseDTO

    @HTTP(method = "GET", path = "core/posts/{post_id}/comments", hasBody = true)
    suspend fun getPostComments(
        @Path("post_id") postId: String,
        @Body request: CommentsGetRequestDTO
    ): CommentListResponseDTO

    @POST("core/posts/{post_id}/like")
    suspend fun likePost(
        @Path("post_id") postId: String
    ): PostLikeResponseDTO

    // FEED
    @HTTP(method = "GET", path = "core/feed", hasBody = true)
    suspend fun getFeed(
        @Body request: GetFeedRequestDTO
    ): PostListResponseDTO

    // FANDOMS
    @POST("core/fandoms/user")
    suspend fun getUserFandoms(
        @Body request: FandomsGetRequestDTO
    ): FandomListResponseDTO

    @GET("core/fandoms/categories")
    suspend fun getFandomCategories(): FandomCategoryListResponseDTO

    @POST("core/fandoms/request-new")
    suspend fun requestNewFandom(
        @Body request: FandomRequestCreateDTO
    ): FandomRequestCreateResponseDTO
}
