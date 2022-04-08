package by.imlab.sosedi.ui.global.helpers

sealed class PrintState {
    object Success : PrintState()
    object Suspense : PrintState()
    object Printing : PrintState()
    object PaperOutError : PrintState()
    object HeadOpenError : PrintState()
    object ConnectionError : PrintState()
    object NotAvailableError : PrintState()
    object InternalError : PrintState()
    object BluetoothError : PrintState()
}