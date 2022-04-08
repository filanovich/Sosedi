package by.imlab.sosedi.ui.scanprinter

sealed class ScanPrinterState {
    object Suspense : ScanPrinterState()
    object Connecting : ScanPrinterState()
    object WrongBarcode : ScanPrinterState()
    object PrinterConnect : ScanPrinterState()
    object PrinterConnectError : ScanPrinterState()
}