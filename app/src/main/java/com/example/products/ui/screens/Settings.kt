package com.example.products.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.traceEventEnd
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.products.ProductsTopAppBar
import com.example.products.R
import com.example.products.data.ProductCurrency
import com.example.products.helpers.Tools
import com.example.products.navigation.NavigationDestination
import com.example.products.ui.AppViewModelFactory


object SettingsDestination : NavigationDestination {
    override val source: String = "settings"
    override val titleRes: Int = R.string.settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory.Factory),
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            ProductsTopAppBar(
                title = stringResource(id = SettingsDestination.titleRes),
                canNavigateBack = true,
                onBack = onBack,
                showExportIcon = true,
                onClickExport = { settingsViewModel.export() }
            )
        }
    ) {
        SettingsBody(
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
            settingsUiState = settingsViewModel.settingsUiState,
            onValueChange = { settingsUiState ->
                settingsViewModel.updateUiState(settingsUiState)
            },
            onSave = {
                settingsViewModel.save()
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "dollarByLbp",
                    settingsViewModel.settingsUiState.dollarByLbp
                )
                onBack()
            }
        )
    }
}

@Composable
fun SettingsBody(
    modifier: Modifier = Modifier,
    currenciesList: List<ProductCurrency> = ProductCurrency.entries,
    settingsUiState: SettingsUiState,
    onValueChange: (SettingsUiState) -> Unit = {},
    onSave: () -> Unit = {}
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.padding_medium)
        )
    ) {
        AnimatedVisibility(visible = settingsUiState.dollarByLbp.isNotBlank()) {
            DollarLbpComponent(
                modifier = Modifier.fillMaxWidth(),
                price = settingsUiState.dollarByLbp,
                priceFontSize = 28,
                labelFontSize = 12
            )
        }

        OutlinedTextField(
            value = settingsUiState.dollarByLbp,
            onValueChange = {
                onValueChange(settingsUiState.copy(dollarByLbp = it))
            },
            label = { Text(text = stringResource(id = R.string.dollar_lbp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen.padding_extra_large)),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Text(text = stringResource(id = R.string.default_settings_title))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(id = R.string.product_profit, settingsUiState.profitRate),
                modifier = Modifier.weight(1f)
            )

            Slider(
                value = settingsUiState.profitRate.toFloat(),
                valueRange = 0f..100f,
                onValueChange = {
                    onValueChange(settingsUiState.copy(profitRate = it.toInt()))
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
                            selected = (settingsUiState.productCurrency == currency),
                            onClick = { onValueChange(settingsUiState.copy(productCurrency = currency)) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (settingsUiState.productCurrency == currency),
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

        Button(
            onClick = onSave,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(id = R.string.save),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                fontStyle = FontStyle.Normal,
            )
        }
    }
}

@Composable
fun DollarLbpComponent(
    modifier: Modifier,
    price: String,
    priceFontSize: Int, labelFontSize: Int
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.padding_small)
        )
    ) {
        Text(
            text = Tools.formatPrice(
                price.toDoubleOrNull() ?: 0.0, ProductCurrency.LBP
            ),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Normal,
            fontSize = priceFontSize.sp
        )
        Text(
            text = stringResource(id = R.string.dollar_lbp),
            fontSize = labelFontSize.sp,
            textAlign = TextAlign.Center,
        )
    }
}