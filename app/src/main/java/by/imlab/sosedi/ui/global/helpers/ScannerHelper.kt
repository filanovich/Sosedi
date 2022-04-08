package by.imlab.sosedi.ui.global.helpers

import androidx.lifecycle.MutableLiveData
import by.imlab.core.extensions.set

class ScannerHelper {

    private val _lastBarcode = MutableLiveData<PrinterBarcode>()
    val lastBarcode = _lastBarcode

    fun updateLastBarcode(barcode: String) {
        _lastBarcode.set(PrinterBarcode(value = barcode))
    }

    companion object {
        const val SCAN_BARCODE = "SCAN_BARCODE1"
        const val SCANNER_RESULT = "nlscan.action.SCANNER_RESULT"
    }

}