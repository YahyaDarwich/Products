package com.example.products.data

import android.database.Cursor
import kotlinx.coroutines.flow.Flow

class OfflineProductsRepository(private val productDao: ProductDao) : ProductsRepository {
    override fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    override fun getAllProductsToExport(): Cursor = productDao.getAllProductsToExport()
    override fun searchProducts(searchKeyword: String): Flow<List<Product>> =
        productDao.searchProducts(searchKeyword)

    override fun getProduct(productId: Int): Flow<Product> = productDao.getProduct(productId)

    override suspend fun addProduct(product: Product) = productDao.addProduct(product)

    override suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    override suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
}