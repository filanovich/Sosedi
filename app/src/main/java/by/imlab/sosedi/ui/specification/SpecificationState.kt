package by.imlab.sosedi.ui.specification

import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities

sealed class SpecificationState {
    object Suspense : SpecificationState()
    data class OrderSuccess(val orderWithEntities: OrderWithEntities) : SpecificationState()
    data class OrderError(val throwable: Throwable) : SpecificationState()
    data class ScanSuccess(
        val skuId: Long,
        val orderNumber: String,
        val barcode: String,
        val allowedSpaces: Int
    ) : SpecificationState()

    object ScanError : SpecificationState()
    object NotEnoughError : SpecificationState()
    data class CollectionSuccess(val order: OrderEntity) : SpecificationState()
    data class CollectionError(val message: String?) : SpecificationState()

    // TODO: Implement the stock balance request feature - SOS-36
    data class StockBalanceSuccess(val value: String?) : SpecificationState()
    data class StockBalanceError(val message: String?) : SpecificationState()

    data class ImageSuccess(val image: String?) : SpecificationState()
    data class ImageError(val message: String?) : SpecificationState()

}