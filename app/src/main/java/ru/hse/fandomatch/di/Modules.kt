package ru.hse.fandomatch.di

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.hse.fandomatch.data.AuthInterceptor
import ru.hse.fandomatch.data.SharedPrefRepositoryImpl
import ru.hse.fandomatch.data.api.ChatApiService
import ru.hse.fandomatch.data.api.CoreApiService
import ru.hse.fandomatch.data.api.S3UploadApiService
import ru.hse.fandomatch.data.api.UserApiService
import ru.hse.fandomatch.data.mock.GlobalRepositoryMock
import ru.hse.fandomatch.data.model.BaseUserProfileDTO
import ru.hse.fandomatch.data.serialization.BaseUserProfileDeserializer
import ru.hse.fandomatch.data.socket.ChatSocketService
import ru.hse.fandomatch.data.socket.ChatSocketServiceImpl
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.auth.ChangeEmailUseCase
import ru.hse.fandomatch.domain.usecase.auth.ChangePasswordUseCase
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatPreviewsUseCase
import ru.hse.fandomatch.domain.usecase.chat.UploadMediaUseCase
import ru.hse.fandomatch.domain.usecase.fandoms.GetFandomsByQueryUseCase
import ru.hse.fandomatch.domain.usecase.fandoms.RequestNewFandomUseCase
import ru.hse.fandomatch.domain.usecase.matches.ApplyFiltersUseCase
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadInitialFiltersUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadSuggestedProfilesUseCase
import ru.hse.fandomatch.domain.usecase.posts.CreatePostUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetFeedUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetFullPostUseCase
import ru.hse.fandomatch.domain.usecase.posts.GetUserPostsUseCase
import ru.hse.fandomatch.domain.usecase.posts.LikePostUseCase
import ru.hse.fandomatch.domain.usecase.posts.SendCommentUseCase
import ru.hse.fandomatch.domain.usecase.auth.CheckVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.DeleteAccountUseCase
import ru.hse.fandomatch.domain.usecase.user.EditProfileUseCase
import ru.hse.fandomatch.domain.usecase.user.GetCitiesByQueryUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendRequestsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendsUseCase
import ru.hse.fandomatch.domain.usecase.auth.GetPastLoginUseCase
import ru.hse.fandomatch.domain.usecase.auth.GetPermissionShownUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserIdUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase
import ru.hse.fandomatch.domain.usecase.auth.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.auth.LoginUseCase
import ru.hse.fandomatch.domain.usecase.auth.LogoutUseCase
import ru.hse.fandomatch.domain.usecase.auth.RegisterUseCase
import ru.hse.fandomatch.domain.usecase.auth.ResetPasswordUseCase
import ru.hse.fandomatch.domain.usecase.auth.SetPermissionShownUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserPreferencesUseCase
import ru.hse.fandomatch.domain.usecase.user.UpdateUserPreferencesUseCase
import ru.hse.fandomatch.ui.addfandom.AddFandomViewModel
import ru.hse.fandomatch.ui.authorization.AuthorizationViewModel
import ru.hse.fandomatch.ui.chat.ChatViewModel
import ru.hse.fandomatch.ui.chatslist.ChatsListViewModel
import ru.hse.fandomatch.ui.editprofile.EditProfileViewModel
import ru.hse.fandomatch.ui.feed.FeedViewModel
import ru.hse.fandomatch.ui.filters.FiltersViewModel
import ru.hse.fandomatch.ui.intro.IntroViewModel
import ru.hse.fandomatch.ui.matches.MatchesViewModel
import ru.hse.fandomatch.ui.newpost.NewPostViewModel
import ru.hse.fandomatch.ui.passwordrecovery.PasswordRecoveryViewModel
import ru.hse.fandomatch.ui.post.PostViewModel
import ru.hse.fandomatch.ui.profile.ProfileViewModel
import ru.hse.fandomatch.ui.registration.RegistrationViewModel
import ru.hse.fandomatch.ui.settings.SettingsViewModel

val appModule = module {
    viewModel<AuthorizationViewModel> { AuthorizationViewModel(loginUseCase = get()) }
    viewModel<RegistrationViewModel> { RegistrationViewModel(
        registerUseCase = get(),
        getVerificationCodeUseCase = get(),
        checkVerificationCodeUseCase = get(),
        uploadMediaUseCase = get(),
        )
    }
    viewModel<IntroViewModel> { IntroViewModel(getPastLoginUseCase = get()) }
    viewModel<MatchesViewModel> {
        MatchesViewModel(
            loadSuggestedProfilesUseCase = get(),
            likeOrDislikeProfileUseCase = get(),
        )
    }
    viewModel<ProfileViewModel> { ProfileViewModel(
        getUserUseCase = get(),
        likeOrDislikeProfileUseCase = get(),
        getUserPostsUseCase = get(),
        getFriendsUseCase = get(),
        getFriendRequestsUseCase = get(),
        likePostUseCase = get(),
    ) }
    viewModel<ChatsListViewModel> { ChatsListViewModel(get()) }
    viewModel<ChatViewModel> { ChatViewModel(get(), get(), get(), get()) }
    viewModel<FiltersViewModel> { FiltersViewModel(get(), get(), get(), get()) }
    viewModel<FeedViewModel> { FeedViewModel(get(), get()) }
    viewModel<EditProfileViewModel> { EditProfileViewModel(get(), get(), get(), get(), get()) }
    viewModel<SettingsViewModel> { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel<AddFandomViewModel> { AddFandomViewModel(get(), get()) }
    viewModel<NewPostViewModel> { NewPostViewModel(get(), get(), get()) }
    viewModel<PasswordRecoveryViewModel> { PasswordRecoveryViewModel(get(), get()) }
    viewModel<PostViewModel> { PostViewModel(get(), get(), get(), get()) }
}

val dataModule = module {
    single<GlobalRepository> { GlobalRepositoryMock() }
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(androidContext()) }

    single(named("apiClient")) {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(get<SharedPrefRepository>()))
            .build()
    }

    single(named("s3Client")) {
        OkHttpClient.Builder()
            .build()
    }

    single(named("gson")) {
        GsonBuilder()
            .registerTypeAdapter(
                BaseUserProfileDTO::class.java,
                BaseUserProfileDeserializer()
            )
            .create()
    }

    single(named("apiRetrofit")) {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.106:8000/") // todo damn
            .client(get(named("apiClient")))
            .addConverterFactory(GsonConverterFactory.create(get(named("gson"))))
            .build()
    }

    single<ChatSocketService> {
        ChatSocketServiceImpl(
            okHttpClient = get(named("apiClient")),
            gson = get(named("gson")),
            wsBaseUrl = "ws://192.168.0.106:8000" // todo ws host
        )
    }

    single(named("s3Retrofit")) {
        Retrofit.Builder()
            .baseUrl("https://s3.amazonaws.com/")
            .client(get(named("s3Client")))
            .addConverterFactory(GsonConverterFactory.create(get(named("gson"))))
            .build()
    }

    single<CoreApiService> { get<Retrofit>(named("apiRetrofit")).create(CoreApiService::class.java) }
    single<UserApiService> { get<Retrofit>(named("apiRetrofit")).create(UserApiService::class.java) }
    single<ChatApiService> { get<Retrofit>(named("apiRetrofit")).create(ChatApiService::class.java) }
    single<S3UploadApiService> { get<Retrofit>(named("s3Retrofit")).create(S3UploadApiService::class.java) }

//    single<GlobalRepository> { GlobalRepositoryImpl(get(), get(), get(), get()) }
    single<GlobalRepository> { GlobalRepositoryMock() }
}

val domainModule = module {
    factory<ChangeEmailUseCase> { ChangeEmailUseCase(get()) }
    factory<ChangePasswordUseCase> { ChangePasswordUseCase(get()) }
    factory<CheckVerificationCodeUseCase> { CheckVerificationCodeUseCase(get()) }
    factory<DeleteAccountUseCase> { DeleteAccountUseCase(get(), get()) }
    factory<GetPastLoginUseCase> { GetPastLoginUseCase(get()) }
    factory<GetPermissionShownUseCase> { GetPermissionShownUseCase(get()) }
    factory<GetVerificationCodeUseCase> { GetVerificationCodeUseCase(get()) }
    factory<LoginUseCase> { LoginUseCase(get(), get()) }
    factory<LogoutUseCase> { LogoutUseCase(get()) }
    factory<RegisterUseCase> { RegisterUseCase(get(), get()) }
    factory<ResetPasswordUseCase> { ResetPasswordUseCase(get()) }
    factory<SetPermissionShownUseCase> { SetPermissionShownUseCase(get()) }

    factory<LoadChatInfoUseCase> { LoadChatInfoUseCase(get()) }
    factory<SendMessageUseCase> { SendMessageUseCase(get()) }
    factory<SubscribeToChatMessagesUseCase> { SubscribeToChatMessagesUseCase(get()) }
    factory<SubscribeToChatPreviewsUseCase> { SubscribeToChatPreviewsUseCase(get()) }
    factory<UploadMediaUseCase> { UploadMediaUseCase(get()) }

    factory<GetFandomsByQueryUseCase> { GetFandomsByQueryUseCase(get()) }
    factory<RequestNewFandomUseCase> { RequestNewFandomUseCase(get(), get()) }

    factory<ApplyFiltersUseCase> { ApplyFiltersUseCase(get(), get()) }
    factory<LikeOrDislikeProfileUseCase> { LikeOrDislikeProfileUseCase(get()) }
    factory<LoadInitialFiltersUseCase> { LoadInitialFiltersUseCase(get()) }
    factory<LoadSuggestedProfilesUseCase> { LoadSuggestedProfilesUseCase(get()) }

    factory<CreatePostUseCase> { CreatePostUseCase(get()) }
    factory<GetFeedUseCase> { GetFeedUseCase(get(), get()) }
    factory<GetFullPostUseCase> { GetFullPostUseCase(get()) }
    factory<GetUserPostsUseCase> { GetUserPostsUseCase(get(), get()) }
    factory<LikePostUseCase> { LikePostUseCase(get()) }
    factory<SendCommentUseCase> { SendCommentUseCase(get()) }

    factory<EditProfileUseCase> { EditProfileUseCase(get()) }
    factory<GetCitiesByQueryUseCase> { GetCitiesByQueryUseCase(get()) }
    factory<GetFriendRequestsUseCase> { GetFriendRequestsUseCase(get(), get()) }
    factory<GetFriendsUseCase> { GetFriendsUseCase(get(), get()) }
    factory<GetUserIdUseCase> { GetUserIdUseCase(get()) }
    factory<GetUserPreferencesUseCase> { GetUserPreferencesUseCase(get()) }
    factory<GetUserUseCase> { GetUserUseCase(get(), get()) }
    factory<UpdateUserPreferencesUseCase> { UpdateUserPreferencesUseCase(get()) }
}
