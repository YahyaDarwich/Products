package com.example.products.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.products.data.ObserveAsEvents
import com.example.products.ui.core.SnackbarController
import com.example.products.ui.screens.AddProduct
import com.example.products.ui.screens.AddProductDestination
import com.example.products.ui.screens.EditProduct
import com.example.products.ui.screens.EditProductDestination
import com.example.products.ui.screens.Home
import com.example.products.ui.screens.HomeDestination
import com.example.products.ui.screens.Settings
import com.example.products.ui.screens.SettingsDestination
import com.example.products.ui.screens.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun ProductsNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()
    ObserveAsEvents(events = SnackbarController.events, snackBarHostState) { event ->
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            val result = snackBarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.action?.name,
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        modifier = modifier
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination.source,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(HomeDestination.source) {
                Home(
                    navController = navController,
                    onAddProduct = { navController.navigate(AddProductDestination.source) },
                    onClickSettings = { navController.navigate(SettingsDestination.source) },
                    onClickProduct = { navController.navigate("${EditProductDestination.source}/$it") })
            }

            composable(AddProductDestination.source) {
                AddProduct(onBack = { navController.navigateUp() })
            }

            composable(
                EditProductDestination.routeWithArgs,
                arguments = listOf(navArgument(EditProductDestination.PRODUCT_ID_ARG) {
                    type = NavType.IntType
                })
            ) {
                EditProduct(onBack = { navController.navigateUp() })
            }

            composable(SettingsDestination.source) {
                Settings(
                    navController,
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}
