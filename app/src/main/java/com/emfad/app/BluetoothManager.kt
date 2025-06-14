package com.emfad.app

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.emfad.app.Models.MeasurementData
import com.emfad.app.Models.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices

    private val _measurementData = MutableStateFlow<MeasurementData?>(null)
    val measurementData: StateFlow<MeasurementData?> = _measurementData

    // EMFAD Device UUIDs (Placeholders - replace with actual UUIDs)
    private val EMFAD_SERVICE_UUID: UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb") // Example: Device Information Service
    private val EMFAD_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb") // Example: Manufacturer Name String

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!_scannedDevices.value.contains(result.device)) {
                _scannedDevices.value = _scannedDevices.value + result.device
                Log.d("BluetoothManager", "Found BLE device: ${result.device.name} - ${result.device.address}")
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result ->
                if (!_scannedDevices.value.contains(result.device)) {
                    _scannedDevices.value = _scannedDevices.value + result.device
                    Log.d("BluetoothManager", "Found BLE device (batch): ${result.device.name} - ${result.device.address}")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BluetoothManager", "BLE Scan Failed: $errorCode")
            _connectionState.value = "Scan Failed: $errorCode"
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val deviceName = gatt.device.name ?: gatt.device.address
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BluetoothManager", "Successfully connected to $deviceName")
                    _connectionState.value = "Connected to $deviceName"
                    bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).postDelayed({ // Delay for service discovery
                        gatt.discoverServices()
                    }, 500)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BluetoothManager", "Disconnected from $deviceName")
                    _connectionState.value = "Disconnected"
                    gatt.close()
                    bluetoothGatt = null
                }
                else -> {
                    Log.d("BluetoothManager", "Connection state changed to $newState for $deviceName")
                    _connectionState.value = "Connection State: $newState"
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "Services discovered for ${gatt.device.name}")
                val service = gatt.getService(EMFAD_SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(EMFAD_CHARACTERISTIC_UUID)
                    if (characteristic != null) {
                        Log.d("BluetoothManager", "Found EMFAD characteristic. Enabling notifications...")
                        enableCharacteristicNotifications(gatt, characteristic)
                    } else {
                        Log.e("BluetoothManager", "EMFAD Characteristic not found!")
                    }
                } else {
                    Log.e("BluetoothManager", "EMFAD Service not found!")
                }
            } else {
                Log.e("BluetoothManager", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "Characteristic read: ${characteristic.uuid} -> ${characteristic.value.toHexString()}")
                processCharacteristicData(characteristic)
            } else {
                Log.e("BluetoothManager", "Characteristic read failed: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d("BluetoothManager", "Characteristic changed: ${characteristic.uuid} -> ${characteristic.value.toHexString()}")
            processCharacteristicData(characteristic)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "Characteristic written: ${characteristic.uuid}")
            } else {
                Log.e("BluetoothManager", "Characteristic write failed: $status")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "Descriptor written: ${descriptor.uuid}")
                _connectionState.value = "Notifications Enabled"
            } else {
                Log.e("BluetoothManager", "Descriptor write failed: $status")
            }
        }
    }

    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BluetoothManager", "Bluetooth is not enabled or not available.")
            _connectionState.value = "Bluetooth Not Available"
            return
        }
        _scannedDevices.value = emptyList() // Clear previous scan results
        _connectionState.value = "Scanning..."
        bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
        Log.d("BluetoothManager", "BLE Scan started.")
    }

    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        _connectionState.value = "Scan Stopped"
        Log.d("BluetoothManager", "BLE Scan stopped.")
    }

    fun connectDevice(device: BluetoothDevice) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BluetoothManager", "Bluetooth is not enabled or not available.")
            _connectionState.value = "Bluetooth Not Available"
            return
        }
        stopScan() // Stop scanning before connecting
        _connectionState.value = "Connecting to ${device.name ?: device.address}..."
        device.connectGatt(context, false, gattCallback)
        Log.d("BluetoothManager", "Attempting to connect to ${device.name ?: device.address}")
    }

    fun disconnectDevice() {
        bluetoothGatt?.let { gatt ->
            val deviceName = gatt.device.name ?: gatt.device.address
            Log.d("BluetoothManager", "Disconnecting from $deviceName")
            gatt.disconnect()
        }
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    fun enableCharacteristicNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)

        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")) // Client Characteristic Configuration Descriptor
        descriptor?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(it)
            Log.d("BluetoothManager", "Writing descriptor to enable notifications for ${characteristic.uuid}")
        } ?: Log.e("BluetoothManager", "CCC Descriptor not found for ${characteristic.uuid}")
    }

    private fun processCharacteristicData(characteristic: BluetoothGattCharacteristic) {
        // This is a placeholder for actual data parsing from the EMFAD device.
        // The format of the data (characteristic.value) will depend on the EMFAD device's BLE profile.
        val rawData = characteristic.value
        if (rawData != null && rawData.isNotEmpty()) {
            // Example: Assuming the data is a simple float value for demonstration
            // In a real scenario, you'd parse based on the defined BLE characteristic format.
            try {
                val value = rawData.decodeToString().toFloatOrNull() ?: 0.0f
                val timestamp = LocalDateTime.now()
                val sessionId = "mock_session_id"
                val id = UUID.randomUUID().toString()
                val unit = "Ohms" // Example unit
                val sensorType = "EMFAD"
                val location = LocationData(latitude = 0.0, longitude = 0.0) // Placeholder

                _measurementData.value = MeasurementData(
                    id = id,
                    sessionId = sessionId,
                    timestamp = timestamp,
                    frequency = 0.0, // Placeholder
                    value = value.toDouble(),
                    unit = unit,
                    sensorType = sensorType,
                    location = location
                )
                Log.d("BluetoothManager", "Processed Measurement: $value $unit")
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Error parsing characteristic data: ${e.message}")
            }
        }
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}