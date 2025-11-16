package ru.hse.fandomatch.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.hse.fandomatch.ui.authorization.AuthorizationViewModel

val appModule = module {
    viewModel<AuthorizationViewModel> { AuthorizationViewModel() }
}

val dataModule = module {

}

val domainModule = module {

}