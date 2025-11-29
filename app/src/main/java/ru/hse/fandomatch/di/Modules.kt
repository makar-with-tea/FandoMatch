package ru.hse.fandomatch.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.hse.fandomatch.data.SharedPrefRepositoryImpl
import ru.hse.fandomatch.data.mock.GlobalRepositoryMock
import ru.hse.fandomatch.data.mock.SharedPrefRepositoryMock
import ru.hse.fandomatch.domain.repos.GlobalRepository
import ru.hse.fandomatch.domain.repos.SharedPrefRepository
import ru.hse.fandomatch.domain.usecase.user.GetPastLoginUseCase
import ru.hse.fandomatch.domain.usecase.user.LoginUseCase
import ru.hse.fandomatch.domain.usecase.user.RegisterUseCase
import ru.hse.fandomatch.ui.authorization.AuthorizationViewModel
import ru.hse.fandomatch.ui.intro.IntroViewModel
import ru.hse.fandomatch.ui.registration.RegistrationScreen
import ru.hse.fandomatch.ui.registration.RegistrationViewModel

val appModule = module {
    viewModel<AuthorizationViewModel> { AuthorizationViewModel(loginUseCase = get()) }
    viewModel<RegistrationViewModel> { RegistrationViewModel(registerUseCase = get()) }
    viewModel<IntroViewModel> { IntroViewModel(getPastLoginUseCase = get()) }
}

val dataModule = module {
    single<GlobalRepository> { GlobalRepositoryMock() }
//    single<SharedPrefRepository> { SharedPrefRepositoryImpl(androidContext()) }
    single<SharedPrefRepository> { SharedPrefRepositoryMock() }
}

val domainModule = module {
    factory<LoginUseCase> { LoginUseCase(get(), get()) }
    factory<GetPastLoginUseCase> { GetPastLoginUseCase(get()) }
    factory<RegisterUseCase> { RegisterUseCase(get(), get()) }
}
