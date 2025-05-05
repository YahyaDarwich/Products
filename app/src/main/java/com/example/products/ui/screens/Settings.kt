package com.example.products.ui.screens

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.ProductsTheme
import com.example.products.ProductsTopAppBar
import com.example.products.R
import com.example.products.models.ProductCurrency
import com.example.products.navigation.NavigationDestination
import com.example.products.ui.AppViewModelFactory
import com.example.products.ui.components.AnimatedTextCounter
import com.example.products.ui.components.DollarLbpComponent
import com.example.products.ui.components.LoadingDialog
import com.google.android.gms.auth.api.identity.Identity


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
    val showLoadingState = settingsViewModel.shouldShowLoading.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    settingsViewModel.import(context, uri)
                }
            }
        }

    val authLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                settingsViewModel.updateUiState(
                    Identity.getAuthorizationClient(context)
                        .getAuthorizationResultFromIntent(result.data)
                )
            }
        }

    val settingsAuthState =
        settingsViewModel.events.collectAsState(SettingsAuthResult.Loading).value
    when (settingsAuthState) {
        is SettingsAuthResult.Success -> {
            try {
                val pendingIntent = settingsAuthState.authorizationResult.pendingIntent
                if (pendingIntent != null) {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(pendingIntent).build()
                    authLauncher.launch(intentSenderRequest)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        else -> {}
    }

    Scaffold(
        topBar = {
            ProductsTopAppBar(
                title = stringResource(id = SettingsDestination.titleRes),
                canNavigateBack = true,
                onBack = onBack
            )
        }
    ) {
        if (showLoadingState.value) {
            LoadingDialog()
        }

        SettingsBody(
            modifier = Modifier
                .padding(it)
                .padding(
                    start = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_medium)
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
            },
            onImport = {
                val intent =
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE);type = "text/*"
                    }
                launcher.launch(intent)
            },
            onExport = { settingsViewModel.export(context) },
            onSignIn = {
                settingsViewModel.authorizeDriveAccess(context)
            },
            onBackup = { settingsViewModel.backupFile(context) },
            onCheckAutoBackup = { checked ->
                settingsViewModel.onCheckAutoBackup(
                    context,
                    checked
                )
            },
            isUserAuthorized = settingsViewModel.settingsUiState.isUserAuthorized,
            email = settingsViewModel.settingsUiState.userEmail,
            lastBackupDate = settingsViewModel.settingsUiState.lastBackupDate,
            lastBackupFileSize = settingsViewModel.settingsUiState.lastBackupFileSize,
            isAutoBackupEnabled = settingsViewModel.settingsUiState.isAutoBackupEnabled,
        )
    }
}

@Composable
fun SettingsBody(
    modifier: Modifier = Modifier,
    currenciesList: List<ProductCurrency> = ProductCurrency.entries,
    settingsUiState: SettingsUiState = SettingsUiState(),
    onValueChange: (SettingsUiState) -> Unit = {},
    onSave: () -> Unit = {},
    onImport: () -> Unit = {},
    onExport: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onBackup: () -> Unit = {},
    onCheckAutoBackup: (Boolean) -> Unit = {},
    isUserAuthorized: Boolean = false,
    isAutoBackupEnabled: Boolean = false,
    email: String? = null,
    lastBackupDate: String? = null,
    lastBackupFileSize: String? = null,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(id = R.dimen.padding_medium)),
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
                .padding(
                    bottom = dimensionResource(id = R.dimen.padding_extra_large),
                    top = dimensionResource(
                        id = R.dimen.padding_medium
                    )
                ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.default_settings_section_title),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

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
                AnimatedTextCounter(count = settingsUiState.profitRate, 80, modifier = Modifier)
                Text(text = "%")
            }

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
                modifier = Modifier
                    .weight(1f)
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

        ImportExportSection(
            onImport = onImport,
            onExport = onExport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_extra_large))
        )

        BackupDataSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = dimensionResource(id = R.dimen.padding_extra_large),
                    bottom = dimensionResource(id = R.dimen.padding_medium)
                ),
            onSignIn = onSignIn,
            onBackup = onBackup,
            onCheckAutoBackup = onCheckAutoBackup,
            isUserAuthorized = isUserAuthorized,
            isAutoBackupEnabled = isAutoBackupEnabled,
            email = email,
            lastBackupDate = lastBackupDate,
            lastBackupFileSize = lastBackupFileSize
        )
    }
}

@Composable
fun ImportExportSection(
    modifier: Modifier = Modifier,
    onImport: () -> Unit = {},
    onExport: () -> Unit = {}
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.padding_medium)
        )
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.import_export_section_title),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = dimensionResource(id = R.dimen.padding_mid_large))
            ) {
                Text(text = stringResource(id = R.string.import_title))
                Text(
                    text = stringResource(id = R.string.all_products_will_be_deleted),
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onImport,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = stringResource(id = R.string.import_data),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Normal,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.export_title),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = dimensionResource(id = R.dimen.padding_mid_large))
            )

            Button(
                onClick = onExport,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = stringResource(id = R.string.export_data),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Normal,
                )
            }
        }
    }
}


@Composable
fun BackupDataSection(
    modifier: Modifier = Modifier,
    onSignIn: () -> Unit = {},
    onBackup: () -> Unit = {},
    onCheckAutoBackup: (Boolean) -> Unit = {},
    isUserAuthorized: Boolean = false,
    isAutoBackupEnabled: Boolean = false,
    lastBackupDate: String? = null,
    lastBackupFileSize: String? = null,
    email: String? = null
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.padding_medium)
        )
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.upload_data_title),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        if (isUserAuthorized) {
            if (!email.isNullOrEmpty()) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.google_account))
                    Text(
                        email,
                        fontSize = 13.sp,
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (lastBackupDate.isNullOrEmpty() ||
                    lastBackupFileSize.isNullOrEmpty()
                ) {
                    Text(
                        stringResource(R.string.no_backup_available),
                        fontSize = 13.sp
                    )
                } else {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            stringResource(R.string.last_backup) + " " + lastBackupDate,
                            fontSize = 13.sp
                        )
                        Text(
                            stringResource(R.string.size) + " " + lastBackupFileSize,
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = onBackup,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                ) {
                    Text(
                        text = stringResource(id = R.string.backup),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Normal,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.automatic_daily_backup))
                Switch(checked = isAutoBackupEnabled, onCheckedChange = onCheckAutoBackup)
            }
        } else {
            Image(
                painter = painterResource(R.drawable.android_neutral_rd_su),
                contentDescription = null,
                modifier = Modifier.clickable { onSignIn() }
            )
        }
    }
}

@Preview()
@Composable
private fun PreviewBackupDataSection() {
    ProductsTheme {
        BackupDataSection(isUserAuthorized = true, email = "ydarwich@gmail.com")
    }
}