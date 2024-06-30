package com.example.products.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.products.data.Product
import com.example.products.data.ProductsRepository
import com.example.products.helpers.LocalStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    var searchQuery = _searchQuery.asStateFlow()

    private var _homeUiState = MutableStateFlow(HomeUIState())
    var homeUiState = _homeUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    productsRepository.getAllProducts()
                } else {
                    productsRepository.searchProducts(query)
                }
            }
                .map { HomeUIState(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = HomeUIState()
                )
                .collect { _homeUiState.value = it }
        }
    }

    fun updateSearchQuery(searchKeyword: String) {
        _searchQuery.value = searchKeyword
    }

    suspend fun deleteProduct(product: Product) {
        productsRepository.deleteProduct(product)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUIState(val products: List<Product> = listOf())
