package com.example.products

import android.app.Application
import com.example.products.data.AppContainer
import com.example.products.data.AppDataContainer

class ProductsApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}