package com.ratacorp.ratatouille

import android.app.Application
import com.ratacorp.ratatouille.di.AppContainer

class RatatouilleApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}