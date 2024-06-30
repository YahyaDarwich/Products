package com.example.products.helpers

import com.example.products.data.ProductCurrency
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class Tools {
    companion object {
        fun formatPrice(price: Double, currency: ProductCurrency): String {
            if (currency == ProductCurrency.DOLLAR) {
                return NumberFormat.getCurrencyInstance(Locale.US).format(price)
            } else {
                val lbpFormat = NumberFormat.getCurrencyInstance(Locale("en", "LB"))
                lbpFormat.currency = Currency.getInstance("LBP")
                return lbpFormat.format(price)
            }
        }
    }
}
