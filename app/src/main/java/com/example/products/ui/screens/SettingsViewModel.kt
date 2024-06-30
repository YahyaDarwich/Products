package com.example.products.ui.screens

import android.database.Cursor
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.products.R
import com.example.products.data.ProductCurrency
import com.example.products.data.ProductsRepository
import com.example.products.data.CSVWriter
import com.example.products.helpers.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(
    private val localStorage: LocalStorage,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    var settingsUiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            settingsUiState = SettingsUiState(
                dollarByLbp = localStorage.getString(SettingsKeys.DOLLAR_BY_LBP.keyName) ?: "",
                profitRate = localStorage.getInt(SettingsKeys.PROFIT_RATE.keyName),
                productCurrency = ProductCurrency.valueOf(
                    localStorage.getString(SettingsKeys.PRODUCT_CURRENCY.keyName)
                        ?: ProductCurrency.DOLLAR.name
                )
            )
        }
    }

    fun updateUiState(settingsState: SettingsUiState) {
        settingsUiState = settingsState
    }

    private fun getAllProductsToExport(): Cursor =
        productsRepository.getAllProductsToExport()

    fun save() {
        viewModelScope.launch {
            localStorage.putString(SettingsKeys.DOLLAR_BY_LBP.keyName, settingsUiState.dollarByLbp)
            localStorage.putInt(
                SettingsKeys.PROFIT_RATE.keyName,
                settingsUiState.profitRate
            )
            localStorage.putString(
                SettingsKeys.PRODUCT_CURRENCY.keyName,
                settingsUiState.productCurrency.name
            )
        }
    }

    fun export() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val exportDir = File(Environment.getExternalStorageDirectory(), "")
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }

                val file =
                    File(
                        exportDir,
                        SimpleDateFormat(
                            "dd MM yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date()) + ".csv"
                    )

                try {
                    file.createNewFile()
                    val csvWrite = CSVWriter(FileWriter(file))
                    val curCSV: Cursor = getAllProductsToExport()
                    csvWrite.writeNext(curCSV.columnNames)
                    while (curCSV.moveToNext()) {
                        val arrStr = arrayOfNulls<String>(curCSV.columnCount)
                        for (i in 0 until curCSV.columnCount - 1) arrStr[i] = curCSV.getString(i)
                        csvWrite.writeNext(arrStr)
                    }
                    csvWrite.close()
                    curCSV.close()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            localStorage.context,
                            R.string.data_exported_successfully,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (sqlEx: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            localStorage.context,
                            sqlEx.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}

data class SettingsUiState(
    val dollarByLbp: String = "",
    val profitRate: Int = 0,
    val productCurrency: ProductCurrency = ProductCurrency.DOLLAR
)

enum class SettingsKeys(val keyName: String) {
    DOLLAR_BY_LBP("dollarByLbp"),
    PROFIT_RATE("profitRate"),
    PRODUCT_CURRENCY("productCurrency")
}