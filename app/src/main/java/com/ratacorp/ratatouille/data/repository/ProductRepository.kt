package com.ratacorp.ratatouille.data.repository

import com.ratacorp.ratatouille.data.local.ProductDao
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.model.toDomainProduct
import com.ratacorp.ratatouille.data.model.toEntity
import com.ratacorp.ratatouille.data.remote.FoodApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(
    private val apiService: FoodApiService,
    private val productDao: ProductDao
) {
    suspend fun getProduct(barcode: String): Result<Product> {
        val localProduct = productDao.getProductByBarcode(barcode)
        
        return try {
            // 1. Tenter de récupérer les données fraîches depuis l'API
            val response = apiService.getProduct(barcode)
            if (response.status == 1 && response.product != null) {
                val product = response.product
                // Sauvegarder/Mettre à jour en local
                productDao.insertProduct(product.toEntity())
                Result.success(product)
            } else if (localProduct != null) {
                // Pas trouvé sur l'API mais présent en local (ex: produit supprimé de OFF mais gardé en historique)
                Result.success(localProduct.toDomainProduct())
            } else {
                Result.failure(Exception("Produit non trouvé"))
            }
        } catch (e: Exception) {
            // 2. En cas d'erreur (réseau coupé, etc.)
            if (localProduct != null) {
                // On a le produit en local ! On le renvoie en le marquant explicitement "Offline"
                productDao.updateScanDate(barcode, System.currentTimeMillis())
                Result.success(localProduct.toDomainProduct().copy(isOffline = true))
            } else {
                // Gestion explicite des erreurs HTTP 404 via les exceptions Retrofit ou le contenu de l'erreur
                val errorMessage = if (e is retrofit2.HttpException) {
                    when (e.code()) {
                        404 -> "Produit non trouvé"
                        429 -> "Trop de requêtes : Veuillez patienter un instant avant de scanner à nouveau."
                        else -> e.message ?: "Une erreur serveur est survenue (${e.code()})"
                    }
                } else if (e is java.net.UnknownHostException) {
                    "Mode hors-ligne : Ce produit n'est pas dans votre historique et nécessite une connexion internet pour être scanné."
                } else {
                    e.message ?: "Une erreur est survenue"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomainProduct() }
        }
    }

    fun getAllFavorites(): Flow<List<Product>> {
        return productDao.getAllFavorites().map { entities ->
            entities.map { it.toDomainProduct() }
        }
    }

    suspend fun getAllFavoritesList(): List<Product> {
        return productDao.getFavoritesList().map { it.toDomainProduct() }
    }

    suspend fun refreshProductData(barcode: String): Result<Product> {
        return try {
            val response = apiService.getProduct(barcode)
            if (response.status == 1 && response.product != null) {
                val product = response.product
                // On récupère le statut favori actuel pour ne pas le perdre lors de l'insert
                val currentLocal = productDao.getProductByBarcode(barcode)
                val isFavorite = currentLocal?.isFavorite ?: false
                val favoriteDate = currentLocal?.favoriteDate
                val scanDate = currentLocal?.scanDate ?: System.currentTimeMillis()

                productDao.insertProduct(product.toEntity(scanDate).copy(
                    isFavorite = isFavorite,
                    favoriteDate = favoriteDate
                ))
                Result.success(product)
            } else {
                Result.failure(Exception("Échec de mise à jour"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(product: Product) {
        val newStatus = !product.isFavorite
        val favoriteDate = if (newStatus) System.currentTimeMillis() else null
        productDao.updateFavoriteStatus(product.code, newStatus, favoriteDate)
    }

    suspend fun getBetterAlternative(product: Product): Product? {
        val currentGrade = product.nutritionGrades?.lowercase()
        if (currentGrade != "d" && currentGrade != "e") return null

        val category = product.categoriesTags?.lastOrNull() ?: return null

        // On cherche d'abord un produit noté A, puis B, puis C
        val gradesToTry = listOf("a", "b", "c")
        for (grade in gradesToTry) {
            try {
                val response = apiService.searchProducts(category = category, targetGrade = grade)
                if (response.products.isNotEmpty()) {
                    return response.products.first()
                }
            } catch (e: Exception) {
                // Continuer vers le grade suivant en cas d'erreur réseau
            }
        }
        return null
    }

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
...
    suspend fun deleteProduct(product: Product) {
        if (product.isFavorite) {
            // S'il est en favori, on le retire juste de l'historique en mettant la date de scan à 0
            productDao.updateScanDate(product.code, 0L)
        } else {
            // Sinon, on le supprime complètement de la base
            productDao.deleteProduct(product.toEntity())
        }
    }

    fun searchProductsPaged(category: String): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProductPagingSource(apiService, category) }
        ).flow
    }
}
