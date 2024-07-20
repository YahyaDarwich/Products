package com.example.products.data

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun insertDataRawFormat(query: SupportSQLiteQuery): Boolean?
    fun getAllProductsToExport(): Cursor
    fun searchProducts(searchKeyword: String): Flow<List<Product>>
    fun getProduct(productId: Int): Flow<Product>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun deleteAllProducts()
}