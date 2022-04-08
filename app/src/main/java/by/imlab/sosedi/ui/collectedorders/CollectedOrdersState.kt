package by.imlab.sosedi.ui.collectedorders

import by.imlab.data.database.entity.OrderEntity

sealed class CollectedOrdersState {
    object Suspense : CollectedOrdersState()
    data class OrdersListSuccess(
        val orderList: List<OrderEntity>
    ) : CollectedOrdersState()

    data class OrdersListError(val throwable: Throwable) : CollectedOrdersState()
    data class ScanSuccess(val orderNumber: String) : CollectedOrdersState()
    object ScanError : CollectedOrdersState()
}