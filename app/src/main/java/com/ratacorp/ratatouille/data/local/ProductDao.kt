package com.ratacorp.ratatouille.data.local

import androidx.room.*
import com.ratacorp.ratatouille.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE code = :barcode")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE scanDate > 0 ORDER BY scanDate DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY favoriteDate DESC")
    fun getAllFavorites(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isFavorite = 1")
    suspend fun getFavoritesList(): List<ProductEntity>

    @Query("UPDATE products SET isFavorite = :isFavorite, favoriteDate = :favoriteDate WHERE code = :barcode")
    suspend fun updateFavoriteStatus(barcode: String, isFavorite: Boolean, favoriteDate: Long?)

    @Query("UPDATE products SET scanDate = :scanDate WHERE code = :barcode")
    suspend fun updateScanDate(barcode: String, scanDate: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Delete
    suspend fun deleteProduct(product: ProductEntity): Int
}
