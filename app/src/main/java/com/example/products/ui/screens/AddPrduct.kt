package com.example.products.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.products.ProductsTopAppBar
import com.example.products.R
import com.example.products.data.ProductCurrency
import com.example.products.navigation.NavigationDestination
import com.example.products.ui.AppViewModelFactory
import com.example.products.ui.components.AnimatedTextCounter
import kotlinx.coroutines.launch

object AddProductDestination : NavigationDestination {
    override val source: String = "add_product"
    override val titleRes: Int = R.string.add_product
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProduct(
    onBack: () -> Unit,
    addProductViewModel: AddProductViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            ProductsTopAppBar(
                title = stringResource(id = AddProductDestination.titleRes),
                canNavigateBack = true,
                onBack = onBack
            )
        }
    ) {
        AddProductBody(
            productDetails = addProductViewModel.productUiState.productDetails,
            modifier = Modifier
                .padding(it)
                .padding(
                    start = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_medium),
                    top = dimensionResource(id = R.dimen.padding_medium),
                    bottom = dimensionResource(id = R.dimen.padding_extra_large)
                )
                .fillMaxWidth()
                .verticalScroll(scrollState),
            onValueChange = { productDetails -> addProductViewModel.updateUiState(productDetails) },
            onSave = {
                coroutineScope.launch {
                    addProductViewModel.addProduct()
                    onBack()
                }
            },
            inputsValid = addProductViewModel.productUiState.isInputsValid
        )
    }
}

@Preview
@Composable
fun AddProductBody(
    modifier: Modifier = Modifier,
    productDetails: ProductDetails = ProductDetails(),
    currenciesList: List<ProductCurrency> = ProductCurrency.entries,
    onValueChange: (ProductDetails) -> Unit = {},
    onSave: () -> Unit = {},
    isEditCase: Boolean = false,
    inputsValid: Boolean = false,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        OutlinedTextField(
            value = productDetails.name,
            onValueChange = { onValueChange(productDetails.copy(name = it)) },
            label = { Text(text = stringResource(id = R.string.product_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = productDetails.description,
            onValueChange = { onValueChange(productDetails.copy(description = it)) },
            label = { Text(text = stringResource(id = R.string.product_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            OutlinedTextField(
                value = productDetails.cost,
                onValueChange = {
                    val product = productDetails.copy(cost = it)
                    product.calculatePrice()
                    onValueChange(product)
                },
                label = { Text(text = stringResource(id = R.string.product_cost)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            OutlinedTextField(
                value = productDetails.price,
                onValueChange = {},
                label = { Text(text = stringResource(id = R.string.product_price)) },
                modifier = Modifier.weight(1f),
                enabled = false
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.product_profit_label),
                    modifier = Modifier.padding(end = 4.dp)
                )
                AnimatedTextCounter(count = productDetails.profitRate, 80, modifier = Modifier)
                Text(text = "%")
            }

            Slider(
                value = productDetails.profitRate.toFloat(),
                valueRange = 0f..100f,
                onValueChange = {
                    val product = productDetails.copy(profitRate = it.toInt())
                    product.calculatePrice()
                    onValueChange(product)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.product_currency),
                modifier = Modifier.weight(1f)
            )

            currenciesList.forEach { currency ->
                Row(
                    Modifier
                        .selectable(
                            selected = (productDetails.currency == currency),
                            onClick = { onValueChange(productDetails.copy(currency = currency)) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (productDetails.currency == currency),
                        onClick = null
                    )

                    Text(
                        text = stringResource(id = currency.resId),
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small))
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.required_fields),
            modifier = Modifier
                .alpha(0.8f)
                .padding(top = dimensionResource(id = R.dimen.padding_small)),
            color = Color.Red
        )

        Button(
            onClick = onSave, enabled = inputsValid,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
        ) {
            Text(
                text = stringResource(id = if (isEditCase) R.string.update else R.string.save),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                fontStyle = FontStyle.Normal,
            )
        }
    }
}