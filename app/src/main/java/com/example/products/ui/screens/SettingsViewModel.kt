package com.example.products.ui.screens

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.products.R
import com.example.products.csv_utils.CSVReader
import com.example.products.csv_utils.CSVWriter
import com.example.products.data.Product
import com.example.products.data.ProductCurrency
import com.example.products.data.ProductsRepository
import com.example.products.helpers.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SettingsViewModel(
    private val localStorage: LocalStorage,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    var settingsUiState by mutableStateOf(SettingsUiState())
        private set
    var shouldShowLoading = mutableStateOf(false)

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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName =
                    SimpleDateFormat(
                        "dd MM yyyy HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date()) + ".csv"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createFileUsingMediaStore(fileName);
                } else {
                    createFileLegacy(fileName);
                }
            } catch (sqlEx: Exception) {
                showToast(sqlEx.message, Toast.LENGTH_SHORT)
            }
        }
    }

    private suspend fun createFileLegacy(fileName: String) {
        val exportDir = File(getExternalStorageDirectory(), "")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, fileName)
        file.createNewFile()
        writeCSVFile(FileWriter(file), fileName)
    }

    private suspend fun createFileUsingMediaStore(fileName: String) {
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "text/csv")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }
        val context = localStorage.context

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            values
        )
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                writeCSVFile(OutputStreamWriter(outputStream), fileName)
            }
        }
    }

    private suspend fun writeCSVFile(writer: Writer, fileName: String) {
        val csvWriter = CSVWriter(writer)
        val curCSV: Cursor = getAllProductsToExport()
        csvWriter.writeNext(curCSV.columnNames)
        while (curCSV.moveToNext()) {
            val arrStr = arrayOfNulls<String>(curCSV.columnCount)
            for (i in 0 until curCSV.columnCount) arrStr[i] = curCSV.getString(i)
            csvWriter.writeNext(arrStr)
        }
        csvWriter.close()
        curCSV.close()

        showToast(
            localStorage.context.getString(R.string.data_exported_successfully) + fileName,
            Toast.LENGTH_LONG
        )
    }

    fun import(fileUri: Uri) {
        showLoading()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (fileUri.lastPathSegment.toString().isNotBlank() &&
                    fileUri.lastPathSegment?.endsWith(".csv") == false
                ) {
                    hideLoading()
                    showToast(R.string.not_csv_file, Toast.LENGTH_LONG)
                    return@launch
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    getFileUsingMediaStore(fileUri);
                } else {
                    getFileLegacy(fileUri);
                }
            } catch (e: Exception) {
                hideLoading()
                showToast(e.message, Toast.LENGTH_LONG)
            }
        }
    }

    private suspend fun getFileLegacy(fileUri: Uri) {
        var fileName = fileUri.lastPathSegment
        if (fileName?.contains("primary:") == true) {
            fileName = fileName.replace("primary:", "").replace("home:", "")
        }

        readFileAndImport(FileReader("${getExternalStorageDirectory()}/$fileName"))
    }

    private suspend fun getFileUsingMediaStore(fileUri: Uri) {
        localStorage.context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
            readFileAndImport(BufferedReader(InputStreamReader(inputStream)))
        }
    }

    private suspend fun readFileAndImport(reader: Reader) {
        val csvReader = CSVReader(reader)
        var nextLine: Array<String>?
        var isImported = false
        var isHeader = true

        productsRepository.deleteAllProducts()

        do {
            val entries = mutableListOf<String>()
            nextLine = csvReader.readNext()
            nextLine?.let { line ->
                for (i in line.indices) {
                    if (isHeader) {
                        isHeader = i != line.size - 1
                    } else {
                        entries.add(line[i])
                    }
                }

                if (!isHeader && entries.isNotEmpty()) {
                    productsRepository.addProduct(
                        Product(
                            id = entries[0].toInt(),
                            name = entries[1],
                            description = entries[2],
                            cost = entries[3].toDouble(),
                            profitRate = entries[4].toInt(),
                            price = entries[5].toDouble(),
                            currency = ProductCurrency.valueOf(entries[6])
                        )
                    )

                    isImported = true
                }
            }
        } while ((nextLine) != null)

        hideLoading()
        if (isImported) {
            showToast(R.string.data_imported_successfully, Toast.LENGTH_LONG)
        }
    }

    private fun showLoading() {
        shouldShowLoading.value = true
    }

    private fun hideLoading() {
        shouldShowLoading.value = false
    }

    private suspend fun showToast(message: String?, duration: Int) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                localStorage.context,
                message,
                duration
            ).show()
        }
    }

    private suspend fun showToast(message: Int, duration: Int) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                localStorage.context,
                message,
                duration
            ).show()
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