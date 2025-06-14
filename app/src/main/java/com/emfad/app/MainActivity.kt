package com.emfad.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emfad.app.Utils.PermissionsManager
import com.emfad.app.ViewModels.BluetoothViewModel
import com.emfad.app.ViewModels.MeasurementViewModel
import com.emfad.app.ui.theme.EmfadAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bluetoothViewModel: BluetoothViewModel by viewModels()
    private val measurementViewModel: MeasurementViewModel by viewModels()
    private lateinit var permissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionsManager = PermissionsManager(this)

        setContent {
            EmfadAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current

                    DisposableEffect(Unit) {
                        // Request Bluetooth permissions
                        permissionsManager.requestBluetoothPermissions {
                            if (it.containsValue(false)) {
                                Toast.makeText(context, "Bluetooth permissions are required for BLE functionality.", Toast.LENGTH_LONG).show()
                            } else {
                                checkBluetoothAdapterAndLocation()
                            }
                        }

                        onDispose { /* Cleanup if needed */ }
                    }

                    EmfadApp()
                }
            }
        }
    }

    private fun checkBluetoothAdapterAndLocation() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device.", Toast.LENGTH_LONG).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        if (!permissionsManager.hasLocationPermissions(this)) {
            permissionsManager.requestLocationPermissions {
                if (it.containsValue(false)) {
                    Toast.makeText(this, "Location permissions are required for BLE scanning.", Toast.LENGTH_LONG).show()
                } else {
                    checkLocationService()
                }
            }
        } else {
            checkLocationService()
        }
    }

    private fun checkLocationService() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Location services must be enabled for BLE scanning.", Toast.LENGTH_LONG).show()
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableLocationIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth has been enabled.", Toast.LENGTH_SHORT).show()
                checkBluetoothAdapterAndLocation()
            } else {
                Toast.makeText(this, "Bluetooth was not enabled. BLE functionality may be limited.", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emfad.app.ui.screens.BluetoothScanScreen
import com.emfad.app.ui.screens.MeasurementScreen

@Composable
fun EmfadApp() {
    val navController = rememberNavController()
    val bluetoothViewModel: BluetoothViewModel = hiltViewModel()
    val measurementViewModel: MeasurementViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "bluetooth_scan") {
        composable("bluetooth_scan") {
            BluetoothScanScreen(bluetoothViewModel) {
                // On device connected, navigate to measurement screen
                navController.navigate("measurement_screen")
            }
        }
        composable("measurement_screen") {
            val connectedDevice by bluetoothViewModel.scannedDevices.collectAsState()
            // Pass the first connected device, or handle multiple if applicable
            MeasurementScreen(bluetoothViewModel, measurementViewModel, connectedDevice.firstOrNull())
        }
    }
}