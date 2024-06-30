package com.example.products.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.products.ProductsApplication
import com.example.products.helpers.LocalStorage
import com.example.products.ui.screens.AddProductViewModel
import com.example.products.ui.screens.EditProductViewModel
import com.example.products.ui.screens.HomeViewModel
import com.example.products.ui.screens.SettingsViewModel

object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                productsApplication().container.productsRepository
            )
        }

        initializer {
            AddProductViewModel(
                LocalStorage(productsApplication().applicationContext),
                productsApplication().container.productsRepository
            )
        }

        initializer {
            EditProductViewModel(
                this.createSavedStateHandle(),
                productsApplication().container.productsRepository
            )
        }

        initializer {
            SettingsViewModel(
                LocalStorage(productsApplication().applicationContext),
                productsApplication().container.productsRepository
            )
        }
    }
}

fun CreationExtras.productsApplication(): ProductsApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProductsApplication)