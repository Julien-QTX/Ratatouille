package com.ratacorp.ratatouille.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ratacorp.ratatouille.RatatouilleApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncFavoritesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as RatatouilleApplication
        val repository = app.container.productRepository

        try {
            Log.d("SyncFavoritesWorker", "Début de la synchronisation des favoris...")
            
            // Récupère tous les produits marqués en favoris
            val favorites = repository.getAllFavoritesList()
            
            var successCount = 0
            favorites.forEach { favorite ->
                // Rafraîchit les données depuis l'API OpenFoodFacts
                val result = repository.refreshProductData(favorite.code)
                if (result.isSuccess) successCount++
            }

            Log.d("SyncFavoritesWorker", "Synchronisation terminée : $successCount produits mis à jour.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncFavoritesWorker", "Erreur lors de la synchronisation", e)
            // Réessaie automatiquement jusqu'à 3 fois (configuré via WorkManager)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
