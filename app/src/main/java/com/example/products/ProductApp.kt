package com.example.products

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.products.navigation.ProductsNavGraph

@Composable
fun ProductApp(navController: NavHostController = rememberNavController()) {
    ProductsNavGraph(navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    canNavigateBack: Boolean,
    showSettings: Boolean = false,
    showExportIcon: Boolean = false,
    onBack: () -> Unit = {},
    onClickSettings: () -> Unit = {},
    onClickExport: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                }
            }
        }, actions = {
            if (showSettings) {
                IconButton(onClick = onClickSettings) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = "settings")
                }
            }

            if (showExportIcon) {
                IconButton(onClick = onClickExport) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_import_export_24),
                        contentDescription = "export data"
                    )
                }
            }
        }
    )
}