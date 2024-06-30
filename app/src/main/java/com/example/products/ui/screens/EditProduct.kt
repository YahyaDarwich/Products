package com.example.products.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.products.ProductsTopAppBar
import com.example.products.R
import com.example.products.navigation.NavigationDestination
import com.example.products.ui.AppViewModelFactory
import kotlinx.coroutines.launch

object EditProductDestination : NavigationDestination {
    override val source: String = "edit_product"
    override val titleRes: Int = R.string.edit_product
    const val PRODUCT_ID_ARG = "productId"
    val routeWithArgs = "$source/{$PRODUCT_ID_ARG}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProduct(
    onBack: () -> Unit,
    editProductViewModel: EditProductViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            ProductsTopAppBar(
                title = stringResource(id = EditProductDestination.titleRes),
                onBack = onBack,
                canNavigateBack = true
            )
        }
    ) {
        AddProductBody(
            Modifier
                .padding(it)
                .padding(
                    start = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_medium),
                    top = dimensionResource(id = R.dimen.padding_medium),
                    bottom = dimensionResource(id = R.dimen.padding_extra_large)
                )
                .fillMaxWidth()
                .verticalScroll(scrollState),
            isEditCase = true,
            productDetails = editProductViewModel.productUiState.productDetails,
            onSave = {
                coroutineScope.launch {
                    editProductViewModel.updateProduct()
                    onBack()
                }
            },
            onValueChange = { productDetails -> editProductViewModel.updateUiState(productDetails) },
            inputsValid = editProductViewModel.productUiState.isInputsValid
        )
    }
}