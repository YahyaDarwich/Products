package com.example.products.data

import android.database.Cursor
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun getAllProductsToExport(): Cursor
    fun searchProducts(searchKeyword: String): Flow<List<Product>>
    fun getProduct(productId: Int): Flow<Product>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
}