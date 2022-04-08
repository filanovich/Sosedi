package by.imlab.sosedi.ui.searchprinter

import by.imlab.data.model.Printer

sealed class SearchPrinterState {
    object Suspense : SearchPrinterState()
    object BluetoothEnabled : SearchPrinterState()
    object BluetoothDisabled : SearchPrinterState()
    object PermissionsGranted : SearchPrinterState()
    object PermissionsDenied : SearchPrinterState()
    data class PrinterFetchSuccess(val printer: Printer) : SearchPrinterState()
    object PrinterConnect : SearchPrinterState()
    object PrinterConnectError : SearchPrinterState()
}