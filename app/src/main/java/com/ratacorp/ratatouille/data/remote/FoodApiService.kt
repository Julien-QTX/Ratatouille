package com.ratacorp.ratatouille.data.remote

import com.ratacorp.ratatouille.data.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FoodApiService {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
    }
}
