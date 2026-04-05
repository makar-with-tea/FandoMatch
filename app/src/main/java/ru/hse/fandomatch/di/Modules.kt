package ru.hse.fandomatch.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.hse.fandomatch.data.SharedPrefRepositoryImpl
import ru.hse.fandomatch.data.mock.GlobalRepositoryMock
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatPreviewsUseCase
import ru.hse.fandomatch.domain.usecase.feed.GetFeedUseCase
import ru.hse.fandomatch.domain.usecase.matches.LikeOrDislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadSuggestedProfilesUseCase
import ru.hse.fandomatch.domain.usecase.user.GetPastLoginUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserIdUseCase
import ru.hse.fandomatch.domain.usecase.user.LoginUseCase
import ru.hse.fandomatch.domain.usecase.user.RegisterUseCase
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
            getUserIdUseCase = get(),
            likeOrDislikeProfileUseCase = get(),
        )
    }
    viewModel<ProfileViewModel> { ProfileViewModel(get(), get()) }
    viewModel<ChatsListViewModel> { ChatsListViewModel(get()) }
    viewModel<ChatViewModel> { ChatViewModel(get(), get(), get()) }
    viewModel<FiltersViewModel> { FiltersViewModel() }
    viewModel<FeedViewModel> { FeedViewModel(get()) }
    viewModel<EditProfileViewModel> { EditProfileViewModel() }
    viewModel<SettingsViewModel> { SettingsViewModel() }
    viewModel<AddFandomViewModel> { AddFandomViewModel() }
    viewModel<NewPostViewModel> { NewPostViewModel() }
}

val dataModule = module {
    single<GlobalRepository> { GlobalRepositoryMock() }
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(androidContext()) }
//    single<SharedPrefRepository> { SharedPrefRepositoryMock() }
}

val domainModule = module {
    factory<LoginUseCase> { LoginUseCase(get(), get()) }
    factory<GetPastLoginUseCase> { GetPastLoginUseCase(get()) }
    factory<RegisterUseCase> { RegisterUseCase(get(), get()) }

    factory<GetUserIdUseCase> { GetUserIdUseCase(get(), get()) }
    factory<LoadSuggestedProfilesUseCase> { LoadSuggestedProfilesUseCase(get()) }
    factory<LikeOrDislikeProfileUseCase> { LikeOrDislikeProfileUseCase(get()) }

    factory<SubscribeToChatMessagesUseCase> { SubscribeToChatMessagesUseCase(get()) }
    factory<SendMessageUseCase> { SendMessageUseCase(get()) }
    factory<LoadChatInfoUseCase> { LoadChatInfoUseCase(get()) }
    factory<SubscribeToChatPreviewsUseCase> { SubscribeToChatPreviewsUseCase(get()) }

    factory<GetFeedUseCase> { GetFeedUseCase(get()) }
}
