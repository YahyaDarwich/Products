package com.example.products.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.products.BuildConfig
import com.example.products.R
import com.example.products.csv_utils.CSVManager
import com.example.products.data.AuthRepository
import com.example.products.data.AuthResult
import com.example.products.data.BackupResult
import com.example.products.data.GoogleApiRepository
import com.example.products.data.ProductsRepository
import com.example.products.helpers.LocalStorage
import com.example.products.models.ProductCurrency
import com.example.products.ui.core.SnackbarAction
import com.example.products.ui.core.SnackbarController
import com.example.products.ui.core.SnackbarEvent
import com.example.products.worker.BackupWorker
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val localStorage: LocalStorage,
    private val productsRepository: ProductsRepository,
    private val googleApiRepository: GoogleApiRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    var settingsUiState by mutableStateOf(SettingsUiState())
        private set

    private var _shouldShowLoading = MutableStateFlow(false)
    var shouldShowLoading = _shouldShowLoading.asStateFlow()

    private val _events = Channel<SettingsAuthResult>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsUiState = SettingsUiState(
                dollarByLbp = localStorage.getString(SettingsKeys.DOLLAR_BY_LBP.keyName) ?: "",
                profitRate = localStorage.getInt(SettingsKeys.PROFIT_RATE.keyName),
                productCurrency = ProductCurrency.valueOf(
                    localStorage.getString(SettingsKeys.PRODUCT_CURRENCY.keyName)
                        ?: ProductCurrency.DOLLAR.name
                ),
                isUserAuthorized = localStorage.getBoolean(SettingsKeys.IS_USER_AUTHORIZED.keyName),
                userEmail = localStorage.getString(SettingsKeys.USER_EMAIL.keyName),
                lastBackupDate = localStorage.getString(SettingsKeys.LAST_BACKUP_DATE.keyName),
                lastBackupFileSize = localStorage.getString(SettingsKeys.LAST_BACKUP_FILE_SIZE.keyName),
                isAutoBackupEnabled = localStorage.getBoolean(SettingsKeys.IS_AUT0_BACKUP_ENABLED.keyName)
            )
        }
    }

    fun updateUiState(settingsState: SettingsUiState) {
        settingsUiState = settingsState
    }

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

    private fun showLoading() {
        _shouldShowLoading.value = true
    }

    private fun hideLoading() {
        _shouldShowLoading.value = false
    }

    private suspend fun showSnackbar(
        message: String,
        action: SnackbarAction? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            SnackbarController.sendMessage(SnackbarEvent(message, action))
        }
    }

    fun authorizeDriveAccess(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.authorize(context).collect {
                when (it) {
                    is AuthResult.Success -> {
                        val authorizationResult = it.authorizationResult
                        if (authorizationResult.hasResolution()) {
                            localStorage.putBoolean(SettingsKeys.IS_USER_AUTHORIZED.keyName, false)
                            settingsUiState = settingsUiState.copy(isUserAuthorized = false)
                            _events.send(SettingsAuthResult.Success(authorizationResult))
                        } else {
                            updateUiState(authorizationResult)
                        }
                    }

                    is AuthResult.Error -> it.e.printStackTrace()
                }
            }
        }
    }

    fun updateUiState(authorizationResult: AuthorizationResult) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                localStorage.putBoolean(SettingsKeys.IS_USER_AUTHORIZED.keyName, true)
                settingsUiState = settingsUiState.copy(isUserAuthorized = true)

                val token = authorizationResult.accessToken
                if (!token.isNullOrEmpty()) {
                    localStorage.putString(SettingsKeys.ACCESS_TOKEN.keyName, token)
                }

                authorizationResult.serverAuthCode?.let {
                    val tokenResponse = GoogleAuthorizationCodeTokenRequest(
                        AndroidHttp.newCompatibleTransport(),
                        GsonFactory.getDefaultInstance(),
                        GoogleOAuthConstants.TOKEN_SERVER_URL,
                        BuildConfig.WEB_CLIENT_ID,
                        BuildConfig.WEB_CLIENT_SECRET,
                        it,
                        ""
                    ).execute()

                    localStorage.putString(
                        SettingsKeys.ACCESS_TOKEN.keyName,
                        tokenResponse.accessToken
                    )
                    localStorage.putString(
                        SettingsKeys.REFRESH_TOKEN.keyName,
                        tokenResponse.refreshToken
                    )
                    localStorage.putLong(
                        SettingsKeys.EXPIRATION_TIME.keyName,
                        System.currentTimeMillis() + (tokenResponse.expiresInSeconds * 1000)
                    )
                }

                if (settingsUiState.userEmail.isNullOrEmpty()) {
                    var email: String? = null

                    if (!authorizationResult.toGoogleSignInAccount()?.email.isNullOrEmpty()) {
                        email = authorizationResult.toGoogleSignInAccount()?.email;
                    } else if (!token.isNullOrEmpty()) {
                        val userInfo = googleApiRepository.getUserInfo("Bearer $token")
                        if (userInfo.email.isNotEmpty()) {
                            email = userInfo.email
                        }
                    }

                    if (!email.isNullOrEmpty()) {
                        localStorage.putString(SettingsKeys.USER_EMAIL.keyName, email)
                        settingsUiState = settingsUiState.copy(userEmail = email)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun backupFile(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.backup(context).collect { result ->
                when (result) {
                    is BackupResult.Loading -> showLoading()
                    is BackupResult.Success -> {
                        hideLoading()
                        showSnackbar(
                            message = context.getString(
                                R.string.successfully_backup_data
                            )
                        )

                        settingsUiState = settingsUiState.copy(
                            lastBackupDate = localStorage.getString(SettingsKeys.LAST_BACKUP_DATE.keyName),
                            lastBackupFileSize = localStorage.getString(SettingsKeys.LAST_BACKUP_FILE_SIZE.keyName)
                        )
                    }

                    is BackupResult.Error -> {
                        hideLoading()
                        showSnackbar(
                            message = context.getString(
                                R.string.something_wrong_try_again
                            ),
                            action = SnackbarAction(
                                name = context.getString(R.string.retry),
                                action = {
                                    backupFile(context)
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    fun onCheckAutoBackup(context: Context, checked: Boolean) {
        localStorage.putBoolean(SettingsKeys.IS_AUT0_BACKUP_ENABLED.keyName, checked)
        settingsUiState = settingsUiState.copy(isAutoBackupEnabled = checked)
        if (checked) {
            viewModelScope.launch(Dispatchers.IO) {
                BackupWorker.enqueue(context)
            }
        } else {
            BackupWorker.cancel(context)
        }
    }

    fun export(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            CSVManager.export(context).collect {
                when (it) {
                    is CSVManager.ExportStatus.Success -> {
                        showSnackbar(
                            context.getString(R.string.data_exported_successfully)
                        )
                    }

                    is CSVManager.ExportStatus.Error -> {
                        it.e.message?.let { message ->
                            showSnackbar(message)
                        }
                    }

                    is CSVManager.ExportStatus.Loading -> {}
                }
            }
        }
    }

    fun import(context: Context, fileUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            CSVManager.import(context, fileUri).collect {
                when (it) {
                    is CSVManager.ImportStatus.Loading -> showLoading()
                    is CSVManager.ImportStatus.NotCsvFile -> {
                        hideLoading()
                        showSnackbar(context.getString(R.string.not_csv_file))
                    }

                    is CSVManager.ImportStatus.Success -> {
                        hideLoading()
                        showSnackbar(localStorage.context.getString(R.string.data_imported_successfully))
                    }

                    is CSVManager.ImportStatus.Error -> {
                        hideLoading()
                        it.e.message?.let { message -> showSnackbar(message) }
                    }
                }
            }
        }
    }
}

data class SettingsUiState(
    val dollarByLbp: String = "",
    val profitRate: Int = 0,
    val productCurrency: ProductCurrency = ProductCurrency.DOLLAR,
    val isUserAuthorized: Boolean = false,
    val isAutoBackupEnabled: Boolean = false,
    val userEmail: String? = null,
    val lastBackupDate: String? = null,
    val lastBackupFileSize: String? = null
)

enum class SettingsKeys(val keyName: String) {
    DOLLAR_BY_LBP("dollarByLbp"),
    PROFIT_RATE("profitRate"),
    PRODUCT_CURRENCY("productCurrency"),
    IS_USER_AUTHORIZED("isUserAuthorized"),
    USER_EMAIL("uer_email"),
    ACCESS_TOKEN("access_token"),
    REFRESH_TOKEN("refresh_token"),
    LAST_BACKUP_DATE("last_backup_date"),
    LAST_BACKUP_FILE_SIZE("last_backup_file_size"),
    IS_AUT0_BACKUP_ENABLED("is_auto_backup_enabled"),
    EXPIRATION_TIME("expiration_time")
}

sealed interface SettingsAuthResult {
    data object Loading : SettingsAuthResult
    data class Success(val authorizationResult: AuthorizationResult) : SettingsAuthResult
}