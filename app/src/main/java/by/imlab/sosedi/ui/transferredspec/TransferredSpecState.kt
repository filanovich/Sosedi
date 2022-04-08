package by.imlab.sosedi.ui.transferredspec

import by.imlab.data.database.model.OrderWithEntities

sealed class TransferredSpecState {
    object Suspense : TransferredSpecState()
    data class OrderSuccess(val orderWithEntities: OrderWithEntities) : TransferredSpecState()
    data class OrderError(val throwable: Throwable) : TransferredSpecState()
}