package by.imlab.sosedi.ui.openpocket

sealed class OpenPocketState {
    object Suspense : OpenPocketState()
    data class OrderError(val throwable: Throwable) : OpenPocketState()
    object WrongCodeError : OpenPocketState()
}