package com.ratacorp.ratatouille.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "product") val product: Product?,
    @Json(name = "status") val status: Int,
    @Json(name = "status_verbose") val statusVerbose: String?
)

@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "code") val code: String,
    @Json(name = "product_name") val productName: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "nutrition_grades") val nutritionGrades: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "categories_tags") val categoriesTags: List<String>?
)

@JsonClass(generateAdapter = true)
data class SearchResponse(
    @Json(name = "products") val products: List<Product>,
    @Json(name = "count") val count: Int
)
