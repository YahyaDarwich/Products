package com.example.products.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.products.data.ProductsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditProductViewModel(
    savedStateHandle: SavedStateHandle,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    var productUiState by mutableStateOf(ProductUiState())
        private set

    private val productId: Int = checkNotNull(savedStateHandle[EditProductDestination.PRODUCT_ID_ARG])

    init {
        viewModelScope.launch {
            productUiState = productsRepository.getProduct(productId).filterNotNull().first()
                .toProductUiState(true)
        }
    }

    fun updateUiState(productDetails: ProductDetails) {
        productUiState = ProductUiState(productDetails, validateInputs(productDetails))
    }

    suspend fun updateProduct() {
        if (validateInputs()) {
            productsRepository.updateProduct(productUiState.productDetails.toProduct())
        }
    }

    private fun validateInputs(productDetails: ProductDetails = productUiState.productDetails): Boolean {
        return with(productDetails) {
            name.isNotBlank() && cost.isNotBlank() && price.isNotBlank()
        }
    }
}