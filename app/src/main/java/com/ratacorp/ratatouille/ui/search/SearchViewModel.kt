package com.ratacorp.ratatouille.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<Product>> = _query
        .debounce(500)
        .filter { it.isNotBlank() }
        .flatMapLatest { category ->
            repository.searchProductsPaged(category)
        }
        .cachedIn(viewModelScope)

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _alternative = MutableStateFlow<Product?>(null)
    val alternative = _alternative.asStateFlow()

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
        _alternative.value = null
        if (product != null) {
            viewModelScope.launch {
                _alternative.value = repository.getBetterAlternative(product)
            }
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            repository.toggleFavorite(product)
            if (_selectedProduct.value?.code == product.code) {
                _selectedProduct.value = _selectedProduct.value?.copy(isFavorite = !product.isFavorite)
            }
        }
    }
}
