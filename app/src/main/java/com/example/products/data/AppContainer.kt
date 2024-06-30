package com.example.products.data

import android.content.Context

interface AppContainer {
    val productsRepository: ProductsRepository
}

class AppDataContainer(context: Context) : AppContainer {
    override val productsRepository: ProductsRepository by lazy {
        OfflineProductsRepository(ProductsDatabase.getDatabase(context).productDao())
    }
}