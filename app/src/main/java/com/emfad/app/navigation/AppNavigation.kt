package com.emfad.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object Bluetooth : Screen("bluetooth")
    object GPS : Screen("gps")
    object Measurement : Screen("measurement/{deviceId}") {
        fun createRoute(deviceId: String) = "measurement/$deviceId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            // TODO: Replace with actual MainScreen composable
            // MainScreen(
            //     onNavigateToSettings = {
            //         navController.navigate(Screen.Settings.route)
            //     },
            //     onNavigateToBluetooth = {
            //         navController.navigate(Screen.Bluetooth.route)
            //     }
            // )
        }
        
        // TODO: Add other composable routes as needed
        // composable(Screen.Settings.route) {
        //     SettingsScreen()
        // }
        // composable(Screen.Bluetooth.route) {
        //     BluetoothScreen()
        // }
        // composable(Screen.GPS.route) {
        //     GPSScreen()
        // }
        // composable(Screen.Measurement.route) {
        //     val deviceId = it.arguments?.getString("deviceId")
        //     MeasurementScreen(deviceId = deviceId)
        // }
    }
}

