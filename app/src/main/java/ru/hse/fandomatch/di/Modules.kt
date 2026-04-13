package ru.hse.fandomatch.di

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.hse.fandomatch.data.AuthInterceptor
import ru.hse.fandomatch.data.SharedPrefRepositoryImpl
import ru.hse.fandomatch.data.api.CoreApiService
import ru.hse.fandomatch.data.mock.GlobalRepositoryMock
import ru.hse.fandomatch.data.model.BaseUserProfileDTO
import ru.hse.fandomatch.data.serialization.BaseUserProfileDeserializer
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatPreviewsUseCase
import ru.hse.fandomatch.domain.usecase.feed.GetFeedUseCase
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadSuggestedProfilesUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendRequestsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetFriendsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetPastLoginUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserPostsUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserUseCase
import ru.hse.fandomatch.domain.usecase.user.GetVerificationCodeUseCase
import ru.hse.fandomatch.domain.usecase.user.LoginUseCase
import ru.hse.fandomatch.domain.usecase.user.RegisterUseCase
import ru.hse.fandomatch.domain.usecase.user.ResetPasswordUseCase
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
import ru.hse.fandomatch.ui.profile.ProfileViewModel
import ru.hse.fandomatch.ui.registration.RegistrationViewModel
import ru.hse.fandomatch.ui.settings.SettingsViewModel

val appModule = module {
    viewModel<AuthorizationViewModel> { AuthorizationViewModel(loginUseCase = get()) }
    viewModel<RegistrationViewModel> { RegistrationViewModel(registerUseCase = get()) }
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
    ) }
    viewModel<ChatsListViewModel> { ChatsListViewModel(get()) }
    viewModel<ChatViewModel> { ChatViewModel(get(), get(), get()) }
    viewModel<FiltersViewModel> { FiltersViewModel() }
    viewModel<FeedViewModel> { FeedViewModel(get()) }
    viewModel<EditProfileViewModel> { EditProfileViewModel() }
    viewModel<SettingsViewModel> { SettingsViewModel() }
    viewModel<AddFandomViewModel> { AddFandomViewModel() }
    viewModel<NewPostViewModel> { NewPostViewModel() }
    viewModel<PasswordRecoveryViewModel> { PasswordRecoveryViewModel(get(), get()) }
}

val dataModule = module {
    single<GlobalRepository> { GlobalRepositoryMock() }
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(androidContext()) }
//    single<SharedPrefRepository> { SharedPrefRepositoryMock() }

    single {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(get<SharedPrefRepository>()))
            .build()
    }

    single {
        val gson = GsonBuilder()
            .registerTypeAdapter(
                BaseUserProfileDTO::class.java,
                BaseUserProfileDeserializer()
            )
            .create()

        Retrofit.Builder()
            .baseUrl("http://192.168.0.106:8000/") // todo damn
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    single<CoreApiService> { get<Retrofit>().create(CoreApiService::class.java) }

//    single<GlobalRepository> { GlobalRepositoryImpl(get()) }
    single<GlobalRepository> { GlobalRepositoryMock() }
}

val domainModule = module {
    factory<LoginUseCase> { LoginUseCase(get(), get()) }
    factory<GetPastLoginUseCase> { GetPastLoginUseCase(get()) }
    factory<RegisterUseCase> { RegisterUseCase(get(), get()) }
    factory<GetVerificationCodeUseCase> { GetVerificationCodeUseCase(get()) }
    factory<ResetPasswordUseCase> { ResetPasswordUseCase(get()) }

    factory<GetUserUseCase> { GetUserUseCase(get(), get()) }
    factory<LoadSuggestedProfilesUseCase> { LoadSuggestedProfilesUseCase(get()) }
    factory<LikeOrDislikeProfileUseCase> { LikeOrDislikeProfileUseCase(get()) }
    factory<GetUserPostsUseCase> { GetUserPostsUseCase(get(), get()) }
    factory<GetFriendsUseCase> { GetFriendsUseCase(get()) }
    factory<GetFriendRequestsUseCase> { GetFriendRequestsUseCase(get()) }

    factory<SubscribeToChatMessagesUseCase> { SubscribeToChatMessagesUseCase(get()) }
    factory<SendMessageUseCase> { SendMessageUseCase(get()) }
    factory<LoadChatInfoUseCase> { LoadChatInfoUseCase(get()) }
    factory<SubscribeToChatPreviewsUseCase> { SubscribeToChatPreviewsUseCase(get()) }

    factory<GetFeedUseCase> { GetFeedUseCase(get()) }
}
