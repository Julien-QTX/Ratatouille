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
        return try {
            // 1. Vérifier en local
            val localProduct = productDao.getProductByBarcode(barcode)
            if (localProduct != null) {
                // Mettre à jour la date de scan pour le faire remonter en tête de liste
                val updatedProduct = localProduct.toDomainProduct()
                productDao.insertProduct(updatedProduct.toEntity())
                return Result.success(updatedProduct)
            }

            // 2. Si non trouvé en local, télécharger depuis l'API
            val response = apiService.getProduct(barcode)
            if (response.status == 1 && response.product != null) {
                val product = response.product
                // 3. Sauvegarder en local pour la prochaine fois
                productDao.insertProduct(product.toEntity())
                Result.success(product)
            } else {
                Result.failure(Exception(response.statusVerbose ?: "Produit introuvable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomainProduct() }
        }
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

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }
}
