package com.emfad.app.bluetooth

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.BleManager
import javax.inject.Inject
import android.content.Context
import java.util.UUID

class BluetoothManager @Inject constructor(
    private val context: Context
) : BleManager(context) {

    // EMFAD device UUIDs
    private val EMFAD_SERVICE_UUID = UUID.fromString("0000110A-0000-1000-8000-00805F9B34FB")
    private val DATA_CHARACTERISTIC_UUID = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB")

    // Connection to EMFAD device
    fun connectToDevice(device: BluetoothDevice) {
        connect(device)
            .timeout(10000)
            .retry(3)
            .useAutoConnect(false)
            .enqueue()
    }

    // Receive measurement data
    fun startDataReception() {
        val characteristic = getCharacteristic(EMFAD_SERVICE_UUID, DATA_CHARACTERISTIC_UUID)
        characteristic?.let {
            setNotificationCallback(it)
                .with { device, data ->
                    data.value?.let { rawData ->
                        processReceivedData(rawData)
                    }
                }
            enableNotifications(it).enqueue()
        }
    }

    // Process raw data types
    private fun processReceivedData(rawData: ByteArray) {
        // TODO: Implement data extraction and processing logic
        // This would parse the raw data according to your device's protocol
    }

    // Implementation of connection state listener
    override fun onDeviceDisconnected() {
        // Implement reconnection logic
        // reconnectIfPossible()
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return object : BleManagerGattCallback() {
            override fun isRequiredServiceSupported(gatt: android.bluetooth.BluetoothGatt): Boolean {
                val service = gatt.getService(EMFAD_SERVICE_UUID)
                return service?.getCharacteristic(DATA_CHARACTERISTIC_UUID) != null
            }

            override fun onServicesInvalidated() {
                // Services have been invalidated
            }
        }
    }
}

