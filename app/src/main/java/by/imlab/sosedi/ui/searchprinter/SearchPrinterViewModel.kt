package by.imlab.sosedi.ui.searchprinter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.model.Printer
import by.imlab.sosedi.ui.global.helpers.PrinterHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchPrinterViewModel(private val printerHelper: PrinterHelper) : ViewModel() {

    private val _state = MutableLiveData<SearchPrinterState>().default(SearchPrinterState.Suspense)
    val state: MutableLiveData<SearchPrinterState> get() = _state

    val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let {
                        if (isDevicePrinter(it)) {
                            val printer = Printer(
                                name = device.name ?: "Printer",
                                mac = device.address // MAC address
                            )
                            _state.set(SearchPrinterState.PrinterFetchSuccess(printer))
                        }
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val bluetoothState = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    when (bluetoothState) {
                        BluetoothAdapter.STATE_ON -> {
                            _state.set(SearchPrinterState.BluetoothEnabled)
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            _state.set(SearchPrinterState.BluetoothDisabled)
                        }
                    }
                }
            }
        }
    }

    fun checkActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == SearchPrinterFragment.REQUEST_ENABLE_BT) {
            if (resultCode == SearchPrinterFragment.ENABLE_BT_ALLOW) {
                _state.set(SearchPrinterState.BluetoothEnabled)
            } else if (resultCode == SearchPrinterFragment.ENABLE_BT_DENY) {
                _state.set(SearchPrinterState.BluetoothDisabled)
            }
        }
    }

    fun printerConnect(mac: String) {
        viewModelScope.launch(Dispatchers.IO) {
            printerHelper.connect(mac = mac).collect { isConnected ->
                if (isConnected) {
                    _state.set(SearchPrinterState.PrinterConnect)
                } else {
                    _state.set(SearchPrinterState.PrinterConnectError)
                }
            }
        }
    }

    fun checkDevicesAndConnect(devices: MutableSet<BluetoothDevice>) {
        _state.set(SearchPrinterState.BluetoothEnabled)
        val printers = devices.filter { isDevicePrinter(it) }
        if (printers.isNotEmpty()) {
            printerConnect(mac = printers.first().address)
        }
    }

    fun resetState() {
        _state.set(SearchPrinterState.Suspense)
    }

    companion object {
        private val BLUETOOTH_PRINTER_IDS = listOf(1664, 7936)

        private fun isDevicePrinter(device: BluetoothDevice): Boolean {
            val deviceClass = device.bluetoothClass.deviceClass
            return BLUETOOTH_PRINTER_IDS.contains(deviceClass)
        }
    }
}