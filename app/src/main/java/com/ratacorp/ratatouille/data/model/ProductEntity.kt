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
    val quantity: String?,
    val ingredientsText: String?,
    val novaGroup: Int?,
    val energyKcal: Float?,
    val fat: Float?,
    val sugars: Float?,
    val salt: Float?,
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
        quantity = quantity,
        ingredientsText = ingredientsText,
        novaGroup = novaGroup,
        nutriments = Nutriments(
            energyKcal = energyKcal,
            fat = fat,
            sugars = sugars,
            salt = salt
        ),
        isFavorite = isFavorite,
        isOffline = false // Par défaut, on ne considère pas comme hors-ligne
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
        quantity = quantity,
        ingredientsText = ingredientsText,
        novaGroup = novaGroup,
        energyKcal = nutriments?.energyKcal,
        fat = nutriments?.fat,
        sugars = nutriments?.sugars,
        salt = nutriments?.salt,
        scanDate = scanDate,
        isFavorite = isFavorite,
        favoriteDate = if (isFavorite) System.currentTimeMillis() else null
    )
}
