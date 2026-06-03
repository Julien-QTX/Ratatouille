package com.ratacorp.ratatouille

import android.app.Application
import com.ratacorp.ratatouille.di.AppContainer

class RatatouilleApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
