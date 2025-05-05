package com.example.products.csv_utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import com.example.products.data.ProductsRepository
import com.example.products.models.Product
import com.example.products.models.ProductCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
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

class CSVManager {
    companion object {
        lateinit var productsRepository: ProductsRepository

        suspend fun export(context: Context) =
            channelFlow {
                send(ExportStatus.Loading)

                withContext(Dispatchers.IO) {
                    try {
                        val fileName =
                            SimpleDateFormat(
                                "dd MM yyyy HH:mm:ss",
                                Locale.getDefault()
                            ).format(Date()) + ".csv"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            createFileUsingMediaStore(context, fileName);
                        } else {
                            createFileLegacy(fileName);
                        }

                        send(ExportStatus.Success)
                    } catch (sqlEx: Exception) {
                        send(ExportStatus.Error(sqlEx))
                    }
                }
            }

        private suspend fun createFileLegacy(fileName: String) {
            val exportDir = File(getExternalStorageDirectory(), "")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val file = File(exportDir, fileName)
            withContext(Dispatchers.IO) {
                file.createNewFile()
                writeCSVFile(FileWriter(file))
            }
        }

        private suspend fun createFileUsingMediaStore(context: Context, fileName: String) {
            val values = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "text/csv")
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                values
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    withContext(Dispatchers.IO) {
                        writeCSVFile(OutputStreamWriter(outputStream))
                    }
                }
            }
        }

        fun writeCSVFile(writer: Writer) {
            val csvWriter = CSVWriter(writer)
            val curCSV: Cursor = productsRepository.getAllProductsToExport()
            csvWriter.writeNext(curCSV.columnNames)
            while (curCSV.moveToNext()) {
                val arrStr = arrayOfNulls<String>(curCSV.columnCount)
                for (i in 0 until curCSV.columnCount) arrStr[i] = curCSV.getString(i)
                csvWriter.writeNext(arrStr)
            }
            csvWriter.close()
            curCSV.close()
        }

        suspend fun import(context: Context, fileUri: Uri) =
            channelFlow {
                send(ImportStatus.Loading)

                withContext(Dispatchers.IO) {
                    try {
                        if (fileUri.lastPathSegment.toString().isNotBlank() &&
                            fileUri.lastPathSegment?.endsWith(".csv") == false
                        ) {
                            send(ImportStatus.NotCsvFile)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                getFileUsingMediaStore(context, fileUri);
                            } else {
                                getFileLegacy(fileUri);
                            }
                            send(ImportStatus.Success)
                        }
                    } catch (e: Exception) {
                        send(ImportStatus.Error(e))
                    }
                }
            }

        private suspend fun getFileLegacy(fileUri: Uri) {
            var fileName = fileUri.lastPathSegment
            if (fileName?.contains("primary:") == true) {
                fileName = fileName.replace("primary:", "").replace("home:", "")
            }

            withContext(Dispatchers.IO) {
                readFileAndImport(FileReader("${getExternalStorageDirectory()}/$fileName"))
            }
        }

        private suspend fun getFileUsingMediaStore(context: Context, fileUri: Uri) {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                withContext(Dispatchers.IO) {
                    readFileAndImport(BufferedReader(InputStreamReader(inputStream)))
                }
            }
        }

        private suspend fun readFileAndImport(reader: Reader) {
            val csvReader = CSVReader(reader)
            var nextLine: Array<String>?
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
                    }
                }
            } while ((nextLine) != null)
        }
    }

    sealed interface ExportStatus {
        data object Loading : ExportStatus
        data object Success : ExportStatus
        data class Error(val e: Exception) : ExportStatus
    }

    sealed interface ImportStatus {
        data object Loading : ImportStatus
        data object Success : ImportStatus
        data object NotCsvFile : ImportStatus
        data class Error(val e: Exception) : ImportStatus
    }
}