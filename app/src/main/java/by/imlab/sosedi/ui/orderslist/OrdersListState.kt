package by.imlab.sosedi.ui.orderslist

import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities

sealed class OrdersListState {
    object Suspense : OrdersListState()
    data class OrdersListSuccess(
        val orderList: List<OrderWithEntities>,
        val hasUnderway: Boolean
    ) : OrdersListState()
    data class OrdersListError(val message: String?) : OrdersListState()
    object OrdersListEmpty : OrdersListState()
    //data class OrdersListError(val throwable: Throwable) : OrdersListState()
    data class OrderAccepted(val order: OrderEntity) : OrdersListState()
    data class OrderAcceptError(val message: String?) : OrdersListState()

    data class ScanSuccess(val orderNumber: String) : OrdersListState()
    object ScanError : OrdersListState()

    //object SpotsDialogOff : OrdersListState()
}