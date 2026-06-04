package com.ratacorp.ratatouille.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val code: String,
    val productName: String?,
    val brands: String?,
    val nutritionGrades: String?,
    val imageUrl: String?,
    val categoriesTags: String?, // Stocké sous forme de chaîne séparée par des virgules
    val scanDate: Long,
    val isFavorite: Boolean = false,
    val favoriteDate: Long? = null
)

fun ProductEntity.toDomainProduct(): Product {
    return Product(
        code = code,
        productName = productName,
        brands = brands,
        nutritionGrades = nutritionGrades,
        imageUrl = imageUrl,
        categoriesTags = categoriesTags?.split(","),
        isFavorite = isFavorite,
        isOffline = true // Indique que le produit vient du cache local
    )
}

fun Product.toEntity(scanDate: Long = System.currentTimeMillis()): ProductEntity {
    return ProductEntity(
        code = code,
        productName = productName,
        brands = brands,
        nutritionGrades = nutritionGrades,
        imageUrl = imageUrl,
        categoriesTags = categoriesTags?.joinToString(","),
        scanDate = scanDate,
        isFavorite = isFavorite,
        favoriteDate = if (isFavorite) System.currentTimeMillis() else null
    )
}
