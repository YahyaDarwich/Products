package com.example.products.data

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

class OfflineProductsRepository(private val productsDatabase: ProductsDatabase) :
    ProductsRepository {
    private val productDao by lazy {
        productsDatabase.productDao()
    }

    override fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    override fun insertDataRawFormat(query: SupportSQLiteQuery): Boolean? =
        productDao.insertDataRawFormat(query)

    override fun getAllProductsToExport(): Cursor = productDao.getAllProductsToExport()
    override fun searchProducts(searchKeyword: String): Flow<List<Product>> =
        productDao.searchProducts(searchKeyword)

    override fun getProduct(productId: Int): Flow<Product> = productDao.getProduct(productId)

    override suspend fun addProduct(product: Product) = productDao.addProduct(product)

    override suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    override suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    override suspend fun deleteAllProducts() = productsDatabase.clearAllTables()
}