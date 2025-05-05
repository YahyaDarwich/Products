package com.example.products.helpers

import com.example.products.models.ProductCurrency
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

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

        fun formatFileSize(fileSize: Long): String {
            if (fileSize <= 0) return "0 B"

            val units = arrayOf("B", "KB", "MB", "GB")
            val digitGroups = (log10(fileSize.toDouble()) / 3).toInt()

            val value = fileSize / 10.0.pow(digitGroups * 3)
            val unit = units[digitGroups]

            return String.format(Locale.getDefault(), "%d %s", value.roundToInt(), unit)
        }

        fun formatDate(timestamp: Long, format: String = "dd MMM yyyy hh:mm aa"): String {
            val formatter = SimpleDateFormat("dd MMM yyyy hh:mm aa", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }

        fun formatDate(date: Date, format: String = "dd MMM yyyy hh:mm aa"): String {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            return formatter.format(date)
        }
    }
}
