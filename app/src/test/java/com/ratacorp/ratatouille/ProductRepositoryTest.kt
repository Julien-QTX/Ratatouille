package com.ratacorp.ratatouille

import android.content.Context
import com.ratacorp.ratatouille.data.local.ProductDao
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.model.ProductEntity
import com.ratacorp.ratatouille.data.model.ProductResponse
import com.ratacorp.ratatouille.data.remote.FoodApiService
import com.ratacorp.ratatouille.data.repository.ProductRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException

class ProductRepositoryTest {

    private lateinit var apiService: FoodApiService
    private lateinit var productDao: ProductDao
    private lateinit var context: Context
    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        apiService = mockk()
        productDao = mockk(relaxed = true) // relaxed=true pour ignorer les appels unitaires sans return
        context = mockk(relaxed = true)
        repository = ProductRepository(context, apiService, productDao)
        
        // On mocke les méthodes privées ou coroutines qui ne nous intéressent pas ici
        mockkConstructor(ProductRepository::class)
    }

    @Test
    fun `getProduct returns local product if API fails`() = runTest {
        // Arrange
        val barcode = "12345"
        val localProductEntity = ProductEntity(
            code = barcode, productName = "Local Test", brands = null, 
            nutritionGrades = null, imageUrl = null, categoriesTags = null, 
            quantity = null, ingredientsText = null, novaGroup = null, 
            energyKcal = null, fat = null, sugars = null, salt = null, 
            scanDate = 1000L, isFavorite = false, favoriteDate = null
        )
        
        coEvery { productDao.getProductByBarcode(barcode) } returns localProductEntity
        coEvery { apiService.getProduct(barcode) } throws UnknownHostException("No internet")

        // Act
        val result = repository.getProduct(barcode)

        // Assert
        assertTrue(result.isSuccess)
        val product = result.getOrNull()
        assertNotNull(product)
        assertEquals("Local Test", product?.productName)
        assertTrue(product?.isOffline == true)
        
        // Vérifie qu'on a bien mis à jour la date de scan en local
        coVerify { productDao.updateScanDate(barcode, any()) }
    }

    @Test
    fun `getProduct returns 404 message when product is unknown online and offline`() = runTest {
        // Arrange
        val barcode = "unknown_code"
        val httpException = mockk<retrofit2.HttpException>()
        every { httpException.code() } returns 404

        coEvery { productDao.getProductByBarcode(barcode) } returns null
        coEvery { apiService.getProduct(barcode) } throws httpException

        // Act
        val result = repository.getProduct(barcode)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Produit non trouvé", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteProduct resets scanDate to 0 if product is favorite`() = runTest {
        // Arrange
        val product = Product(
            code = "123", productName = "Fav Product", brands = null, 
            nutritionGrades = null, imageUrl = null, categoriesTags = null, 
            isFavorite = true, isOffline = false
        )

        // Act
        repository.deleteProduct(product)

        // Assert
        // Vérifie qu'on a mis à jour la date au lieu de faire un vrai DELETE SQL
        coVerify { productDao.updateScanDate("123", 0L) }
        coVerify(exactly = 0) { productDao.deleteProduct(any()) }
    }
}
