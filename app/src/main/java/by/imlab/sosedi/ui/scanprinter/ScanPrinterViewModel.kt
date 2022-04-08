package by.imlab.sosedi.ui.scanprinter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.PrinterHelper
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ScanPrinterViewModel(
    private val printerHelper: PrinterHelper,
    scannerHelper: ScannerHelper
) : ViewModel() {

    val printerScanState = MutableLiveData<ScanPrinterState>().default(ScanPrinterState.Suspense)

    val barcode = scannerHelper.lastBarcode

    fun validateAndConnect(barcode: PrinterBarcode) {
        if (validateMac(mac = barcode.value)) {
            printerConnect(barcode.toString())
        } else {
            val formatMac = convertToColonSeparatedMac(barcode.value)
            if (validateMac(mac = formatMac)) {
                printerConnect(barcode.value)
            } else {
                printerScanState.set(ScanPrinterState.WrongBarcode)
            }
        }
    }

    private fun printerConnect(mac: String) {
        printerScanState.set(ScanPrinterState.Connecting)
        viewModelScope.launch(Dispatchers.IO) {
            printerHelper.connect(mac = mac).collect { isConnected ->
                if (isConnected) {
                    printerScanState.set(ScanPrinterState.PrinterConnect)
                } else {
                    printerScanState.set(ScanPrinterState.PrinterConnectError)
                }
            }
        }
    }

    private fun validateMac(mac: String): Boolean {
        return Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})\$").matches(mac)
    }

    private fun convertToColonSeparatedMac(mac: String): String {
        var finals = ""
        var i = 0
        while (i < mac.length) {
            if (i + 2 < mac.length) finals += mac.substring(i, i + 2) + ":"
            if (i + 2 == mac.length) {
                finals += mac.substring(i, i + 2)
            }
            i += 2
        }
        return finals
    }
}