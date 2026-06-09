package com.ratacorp.ratatouille.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.remote.FoodApiService

class ProductPagingSource(
    private val apiService: FoodApiService,
    private val category: String
) : PagingSource<Int, Product>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        val position = params.key ?: 1
        return try {
            val response = apiService.searchProducts(
                category = category,
                page = position,
                pageSize = params.loadSize
            )
            val products = response.products
            val totalProducts = response.count
            val pageSize = params.loadSize
            
            // Calculer s'il y a une page suivante
            // totalProducts / pageSize (arrondi au supérieur) donne le nombre total de pages
            val hasNextPage = (position * pageSize) < totalProducts

            LoadResult.Page(
                data = products,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (hasNextPage && products.isNotEmpty()) position + 1 else null
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
