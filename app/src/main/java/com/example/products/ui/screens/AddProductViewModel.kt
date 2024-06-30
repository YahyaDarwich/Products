package com.example.products.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.products.data.Product
import com.example.products.data.ProductCurrency
import com.example.products.data.ProductsRepository
import com.example.products.helpers.LocalStorage
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale


class AddProductViewModel(
    localStorage: LocalStorage,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    private val settingsUiState by mutableStateOf(
        SettingsUiState(
            dollarByLbp = localStorage.getString(SettingsKeys.DOLLAR_BY_LBP.keyName) ?: "",
            profitRate = localStorage.getInt(SettingsKeys.PROFIT_RATE.keyName),
            productCurrency = ProductCurrency.valueOf(
                localStorage.getString(SettingsKeys.PRODUCT_CURRENCY.keyName)
                    ?: ProductCurrency.DOLLAR.name
            )
        )
    )

    var productUiState by mutableStateOf(
        ProductUiState(
            productDetails = ProductDetails(
                profitRate = settingsUiState.profitRate,
                currency = settingsUiState.productCurrency
            )
        )
    )
        private set


    fun updateUiState(productDetails: ProductDetails) {
        productUiState = ProductUiState(productDetails, validateInputs(productDetails))
    }

    suspend fun addProduct() {
        if (validateInputs()) {
            productsRepository.addProduct(productUiState.productDetails.toProduct())
        }
    }

    private fun validateInputs(productDetails: ProductDetails = productUiState.productDetails): Boolean {
        return with(productDetails) {
            name.isNotBlank() && cost.isNotBlank() && price.isNotBlank()
        }
    }
}

data class ProductUiState(
    val productDetails: ProductDetails = ProductDetails(),
    val isInputsValid: Boolean = false
)

fun Product.toProductUiState(isInputsValid: Boolean = false): ProductUiState =
    ProductUiState(this.toProductDetails(), isInputsValid)

fun Product.formatPrice(
    price: Double = this.price,
    currency: ProductCurrency = this.currency
): String {
    if (currency == ProductCurrency.DOLLAR) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(price)
    } else {
        val lbpFormat = NumberFormat.getCurrencyInstance(Locale("en", "LB"))
        lbpFormat.currency = Currency.getInstance("LBP")
        return lbpFormat.format(price)
    }
}

fun Product.getFormattedPriceByDollar(dollarByLbp: String?): String {
    val byLbp = dollarByLbp?.toDoubleOrNull()
    return if (currency == ProductCurrency.DOLLAR && byLbp != null && byLbp != 0.0)
        formatPrice(price * byLbp, ProductCurrency.LBP)
    else formatPrice()
}

fun ProductDetails.toProduct(): Product =
    Product(
        id = id,
        name = name,
        description = description,
        cost = cost.toDoubleOrNull() ?: 0.0,
        profitRate = profitRate,
        price = price.toDoubleOrNull() ?: 0.0,
        currency = currency
    )

fun ProductDetails.calculatePrice() {
    if (this.cost.isNotBlank()) {
        this.price = String.format(
            Locale.getDefault(),
            "%.2f",
            this.cost.toDouble() * (1 + this.profitRate / 100f)
        )
    } else this.price = ""
}

fun Product.toProductDetails(): ProductDetails =
    ProductDetails(
        id = id,
        name = name,
        description = description,
        cost = cost.toString(),
        profitRate = profitRate,
        price = price.toString(),
        currency = currency
    )

data class ProductDetails(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val cost: String = "",
    val profitRate: Int = 0,
    var price: String = "",
    val currency: ProductCurrency = ProductCurrency.DOLLAR
)