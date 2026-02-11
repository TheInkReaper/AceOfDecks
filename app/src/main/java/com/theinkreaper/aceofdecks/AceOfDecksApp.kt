package com.theinkreaper.aceofdecks

import android.app.Application
import com.theinkreaper.aceofdecks.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AceOfDecksApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@AceOfDecksApp)
            modules(appModule)
        }
    }
}