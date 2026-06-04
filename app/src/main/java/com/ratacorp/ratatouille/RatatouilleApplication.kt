package com.ratacorp.ratatouille

import android.app.Application
import com.ratacorp.ratatouille.di.AppContainer

import androidx.work.*
import com.ratacorp.ratatouille.data.worker.SyncFavoritesWorker
import java.util.concurrent.TimeUnit

class RatatouilleApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        
        scheduleBackgroundSync()
    }

    private fun scheduleBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncFavoritesWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncFavoritesWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
