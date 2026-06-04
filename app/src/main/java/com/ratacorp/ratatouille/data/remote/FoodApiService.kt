package com.ratacorp.ratatouille.data.remote

import com.ratacorp.ratatouille.data.model.ProductResponse
import com.ratacorp.ratatouille.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApiService {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse

    @GET("cgi/search.pl?action=process&json=true")
    suspend fun searchProducts(
        @Query("tagtype_0") tagType0: String = "categories",
        @Query("tag_contains_0") tagContains0: String = "contains",
        @Query("tag_0") category: String,
        @Query("sort_by") sortBy: String = "nutriscore_score",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): SearchResponse

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
    }
}
