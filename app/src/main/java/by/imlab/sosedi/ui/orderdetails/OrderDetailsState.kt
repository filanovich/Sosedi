package by.imlab.sosedi.ui.orderdetails

import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities

sealed class OrderDetailsState {
    object Suspense : OrderDetailsState()
    object Ordering : OrderDetailsState()
    data class OrderSuccess(val orderWithEntities: OrderWithEntities) : OrderDetailsState()
    data class OrderError(val throwable: Throwable) : OrderDetailsState()
    data class ScanSuccess(
        val skuId: Long,
        val orderNumber: String,
        val barcode: String,
        val allowedSpaces: Int
    ) : OrderDetailsState()

    data class ScanError(val error: OrderDetailsError) : OrderDetailsState()
    data class CollectionSuccess(val order: OrderEntity) : OrderDetailsState()
    data class CollectionError(val message: String?) : OrderDetailsState()
}