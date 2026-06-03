package com.ratacorp.ratatouille.di

import android.content.Context
import com.ratacorp.ratatouille.data.local.AppDatabase
import com.ratacorp.ratatouille.data.remote.FoodApiService
import com.ratacorp.ratatouille.data.repository.ProductRepository
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(private val context: Context) {
    private val moshi = Moshi.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(FoodApiService.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val foodApiService: FoodApiService by lazy {
        retrofit.create(FoodApiService::class.java)
    }

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(foodApiService, database.productDao())
    }
}
