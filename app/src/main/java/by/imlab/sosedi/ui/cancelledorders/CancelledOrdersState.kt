package by.imlab.sosedi.ui.cancelledorders

import by.imlab.data.database.entity.OrderEntity

sealed class CancelledOrdersState {
    object Suspense : CancelledOrdersState()
    data class OrdersListSuccess(
        val orderList: List<OrderEntity>
    ) : CancelledOrdersState()

    data class OrdersListError(val throwable: Throwable) : CancelledOrdersState()
    data class ScanSuccess(val orderNumber: String) : CancelledOrdersState()
    object ScanError : CancelledOrdersState()
}