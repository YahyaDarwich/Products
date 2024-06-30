package com.example.products.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.products.R

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val description: String,
    val cost: Double,
    val profitRate: Int,
    val price: Double,
    val currency: ProductCurrency
)

enum class ProductCurrency(val resId: Int) {
    DOLLAR(R.string.dollar_currency), LBP(R.string.lbp_currency)
}