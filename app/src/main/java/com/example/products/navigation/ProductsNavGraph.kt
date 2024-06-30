package com.example.products.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.products.ui.screens.AddProduct
import com.example.products.ui.screens.AddProductDestination
import com.example.products.ui.screens.EditProduct
import com.example.products.ui.screens.EditProductDestination
import com.example.products.ui.screens.Home
import com.example.products.ui.screens.HomeDestination
import com.example.products.ui.screens.Settings
import com.example.products.ui.screens.SettingsDestination

@Composable
fun ProductsNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.source,
        modifier = modifier
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
            Settings(navController, onBack = { navController.navigateUp() })
        }
    }
}