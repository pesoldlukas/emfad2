
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.ByteArrayData
import javax.inject.Inject

class BluetoothService @Inject constructor(
    private val context: Context
) : BleManager<BluetoothDevice, EMFADMeasurement>(context) {

    // UUIDs für EMFAD-Gerät
    private val EMFAD_SERVICE_UUID = UUID.fromString("0000110A-0000-1000-8000-00805F9B34FB")
    private val EMFAD_CHARACTERISTIC_UUID = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB")

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _measurementData = MutableSharedFlow<EMFADMeasurement>()
    val measurementData: SharedFlow<EMFADMeasuremen

