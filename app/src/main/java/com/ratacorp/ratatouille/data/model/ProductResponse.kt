package com.ratacorp.ratatouille.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "product") val product: Product?,
    @Json(name = "status") val status: Int,
    @Json(name = "status_verbose") val statusVerbose: String?
)
