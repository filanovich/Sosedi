package by.imlab.sosedi.ui.transferredorders

import by.imlab.data.database.entity.OrderEntity

sealed class TransferredOrdersOrdersState {
    object Suspense : TransferredOrdersOrdersState()
    data class OrdersListSuccess(
        val orderList: List<OrderEntity>
    ) : TransferredOrdersOrdersState()

    data class OrdersListError(val throwable: Throwable) : TransferredOrdersOrdersState()
    data class ScanSuccess(val orderNumber: String) : TransferredOrdersOrdersState()
    object ScanError : TransferredOrdersOrdersState()
}