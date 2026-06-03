package com.ratacorp.ratatouille.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ratacorp.ratatouille.data.repository.ProductRepository
import com.ratacorp.ratatouille.ui.history.HistoryViewModel
import com.ratacorp.ratatouille.ui.scan.ScanViewModel

class ViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ScanViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HistoryViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
