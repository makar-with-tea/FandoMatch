package ru.hse.fandomatch.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.hse.fandomatch.data.SharedPrefRepositoryImpl
import ru.hse.fandomatch.data.mock.GlobalRepositoryMock
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.chat.LoadChatInfoUseCase
import ru.hse.fandomatch.domain.usecase.chat.SubscribeToChatPreviewsUseCase
import ru.hse.fandomatch.domain.usecase.chat.LoadMessagesUseCase
import ru.hse.fandomatch.domain.usecase.chat.SendMessageUseCase
import ru.hse.fandomatch.domain.usecase.feed.GetFeedUseCase
import ru.hse.fandomatch.domain.usecase.matches.DislikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LikeProfileUseCase
import ru.hse.fandomatch.domain.usecase.matches.LoadSuggestedProfilesUseCase
import ru.hse.fandomatch.domain.usecase.user.GetPastLoginUseCase
import ru.hse.fandomatch.domain.usecase.user.GetUserIdUseCase
import ru.hse.fandomatch.domain.usecase.user.LoginUseCase
import ru.hse.fandomatch.domain.usecase.user.RegisterUseCase
import ru.hse.fandomatch.ui.myprofile.ProfileViewModel
import ru.hse.fandomatch.ui.authorization.AuthorizationViewModel
import ru.hse.fandomatch.ui.chat.ChatViewModel
import ru.hse.fandomatch.ui.chatslist.ChatsListViewModel
import ru.hse.fandomatch.ui.editprofile.EditProfileViewModel
import ru.hse.fandomatch.ui.feed.FeedViewModel
import ru.hse.fandomatch.ui.filters.FiltersViewModel
import ru.hse.fandomatch.ui.intro.IntroViewModel
import ru.hse.fandomatch.ui.matches.MatchesViewModel
import ru.hse.fandomatch.ui.registration.RegistrationViewModel

val appModule = module {
    viewModel<AuthorizationViewModel> { AuthorizationViewModel(loginUseCase = get()) }
    viewModel<RegistrationViewModel> { RegistrationViewModel(registerUseCase = get()) }
    viewModel<IntroViewModel> { IntroViewModel(getPastLoginUseCase = get()) }
    viewModel<MatchesViewModel> {
        MatchesViewModel(
            loadSuggestedProfilesUseCase = get(),
            getUserIdUseCase = get(),
            likeProfileUseCase = get(),
            dislikeProfileUseCase = get()
        )
    }
    viewModel<ProfileViewModel> { ProfileViewModel() }
    viewModel<ChatsListViewModel> { ChatsListViewModel(get()) }
    viewModel<ChatViewModel> { ChatViewModel(get(), get()) }
    viewModel<FiltersViewModel> { FiltersViewModel() }
    viewModel<FeedViewModel> { FeedViewModel(get()) }
    viewModel<EditProfileViewModel> { EditProfileViewModel() }
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
    factory<LikeProfileUseCase> { LikeProfileUseCase(get()) }
    factory<DislikeProfileUseCase> { DislikeProfileUseCase(get()) }

    factory<LoadMessagesUseCase> { LoadMessagesUseCase(get()) }
    factory<SendMessageUseCase> { SendMessageUseCase(get()) }
    factory<LoadChatInfoUseCase> { LoadChatInfoUseCase(get()) }
    factory<SubscribeToChatPreviewsUseCase> { SubscribeToChatPreviewsUseCase(get()) }

    factory<GetFeedUseCase> { GetFeedUseCase(get()) }
}
