package com.example.products.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.products.R
import com.example.products.models.ProductCurrency
import com.example.products.helpers.Tools

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