package ru.hse.fandomatch

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.hse.fandomatch.di.appModule
import ru.hse.fandomatch.di.dataModule
import ru.hse.fandomatch.di.domainModule

class FandoMatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FandoMatchApplication)
            modules(
                appModule,
                dataModule,
                domainModule
            )
        }
    }
}
