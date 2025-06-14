package com.emfad.app.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emfad.app.ViewModels.BluetoothViewModel

@Composable
fun BluetoothScanScreen(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
    onDeviceConnected: (BluetoothDevice) -> Unit
) {
    val connectionState by bluetoothViewModel.connectionState.collectAsState()
    val scannedDevices by bluetoothViewModel.scannedDevices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connection State: $connectionState",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (connectionState == "Scanning...") {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        Button(onClick = { bluetoothViewModel.startScan() }) {
            Text("Start Scan")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { bluetoothViewModel.stopScan() }) {
            Text("Stop Scan")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Found Devices:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(scannedDevices) { device ->
                DeviceItem(device = device) {
                    bluetoothViewModel.connectDevice(it)
                    onDeviceConnected(it)
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: BluetoothDevice, onConnectClick: (BluetoothDevice) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onConnectClick(device) },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${device.name ?: "Unknown Device"}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Address: ${device.address}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}