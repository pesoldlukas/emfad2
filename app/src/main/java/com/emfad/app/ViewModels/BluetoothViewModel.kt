package com.emfad.app.ViewModels

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emfad.app.BluetoothManager
import com.emfad.app.Models.MeasurementData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    application: Application,
    private val bluetoothManager: BluetoothManager
) : AndroidViewModel(application) {

    val connectionState: StateFlow<String> = bluetoothManager.connectionState
    val scannedDevices: StateFlow<List<BluetoothDevice>> = bluetoothManager.scannedDevices
    val measurementData: StateFlow<MeasurementData?> = bluetoothManager.measurementData

    fun startScan() {
        viewModelScope.launch {
            bluetoothManager.startScan()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            bluetoothManager.stopScan()
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            bluetoothManager.connectDevice(device)
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            bluetoothManager.disconnectDevice()
        }
    }
}