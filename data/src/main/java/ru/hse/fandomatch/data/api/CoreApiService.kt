package ru.hse.fandomatch.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.hse.fandomatch.data.model.CommentListResponseDTO
import ru.hse.fandomatch.data.model.CreatePostRequestDTO
import ru.hse.fandomatch.data.model.CreatePostResponseDTO
import ru.hse.fandomatch.data.model.EditUserProfileRequestDTO
import ru.hse.fandomatch.data.model.EditUserProfileResponseDTO
import ru.hse.fandomatch.data.model.FandomCategoryListResponseDTO
import ru.hse.fandomatch.data.model.FandomListResponseDTO
import ru.hse.fandomatch.data.model.FandomRequestCreateDTO
import ru.hse.fandomatch.data.model.FandomRequestCreateResponseDTO
import ru.hse.fandomatch.data.model.FandomsGetRequestDTO
import ru.hse.fandomatch.data.model.MatchActionRequestDTO
import ru.hse.fandomatch.data.model.MatchActionResponseDTO
import ru.hse.fandomatch.data.model.MatchBatchRequestDTO
import ru.hse.fandomatch.data.model.MatchCandidateBatchResponseDTO
import ru.hse.fandomatch.data.model.MatchFilterRequestDTO
import ru.hse.fandomatch.data.model.MatchFilterResponseDTO
import ru.hse.fandomatch.data.model.PostLikeResponseDTO
import ru.hse.fandomatch.data.model.PostListResponseDTO
import ru.hse.fandomatch.data.model.PostsGetRequestDTO
import ru.hse.fandomatch.data.model.UserProfileRequestDTO
import ru.hse.fandomatch.data.model.UserProfileResponseDTO

interface CoreApiService {
    // todo даша: обсудили в чате, поменять сущности
    @POST("core/user/profile")
    suspend fun getUserProfile(
        @Body request: UserProfileRequestDTO
    ): UserProfileResponseDTO

    // todo даша: гендер и город енамом; birthDate мы не меняем так что если ты будешь объединять два запроса регистрации в один то тут оно будет не нужно
    @PATCH("core/user/profile/edit")
    suspend fun editUserProfile(
        @Body request: EditUserProfileRequestDTO
    ): EditUserProfileResponseDTO

    // MATCHING
    // todo даша: сделать фандомы сущностями, а еще поля non-null, если можно
    @POST("core/match/next")
    suspend fun getMatchCandidates(
        @Body request: MatchBatchRequestDTO
    ): MatchCandidateBatchResponseDTO

    // todo даша: заменить в запросе username на id
    @POST("core/match/react")
    suspend fun reactOnMatch(
        @Body request: MatchActionRequestDTO
    ): MatchActionResponseDTO

    // todo даша: сделать гендеры и категории списком енамов, для фандомов тоже список, сделать город булевым
    @POST("core/match/filter")
    suspend fun setMatchFilter(
        @Body request: MatchFilterRequestDTO
    ): MatchFilterResponseDTO

    // todo даша: добавить запрос на получение текущих фильтров

    // POSTS
    // todo даша: обновить сущность поста (см. тг)

    // todo даша: может id вместо username в запросе?
    @POST("core/posts/get")
    suspend fun getPosts(
        @Body request: PostsGetRequestDTO
    ): PostListResponseDTO

    // todo даша: добавить возможность прикреплять к посту картинку (параметр imageUrl?)
    @POST("core/posts/create")
    suspend fun createPost(
        @Body request: CreatePostRequestDTO
    ): CreatePostResponseDTO

    // todo даша: а зачем оно?
    @GET("core/posts/{post_id}")
    suspend fun getPost(
        @Path("post_id") postId: String
    ): CreatePostResponseDTO

    // todo даша: в комментах нужны аватар+логин+имя пользователя, аналогично постам
    @GET("core/posts/{post_id}/comments")
    suspend fun getPostComments(
        @Path("post_id") postId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): CommentListResponseDTO

    // todo даша: добавить возможность убрать свой лайк (параметр isLike?)
    @POST("core/posts/{post_id}/like")
    suspend fun likePost(
        @Path("post_id") postId: String
    ): PostLikeResponseDTO

    // FEED
    // todo даша: памагите как работают страницы
    @GET("core/feed")
    suspend fun getFeed(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PostListResponseDTO

    // FANDOMS
    // todo даша: обновить сущность фандома, заменить в запросе username на id
    @POST("core/fandoms/user")
    suspend fun getUserFandoms(
        @Body request: FandomsGetRequestDTO
    ): FandomListResponseDTO

    // todo даша: оно нам не надо
    @GET("core/fandoms/all")
    suspend fun getAllFandoms(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): FandomListResponseDTO

    // todo даша: сделать категории енамом
    @GET("core/fandoms/categories")
    suspend fun getFandomCategories(): FandomCategoryListResponseDTO

    // todo даша: заменить в запросе username на id, сделать категорию енамом
    @POST("core/fandoms/request-new")
    suspend fun requestNewFandom(
        @Body request: FandomRequestCreateDTO
    ): FandomRequestCreateResponseDTO
}
