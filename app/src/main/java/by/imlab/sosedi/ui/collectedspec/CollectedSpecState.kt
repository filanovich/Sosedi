package by.imlab.sosedi.ui.collectedspec

import by.imlab.data.database.model.OrderWithEntities

sealed class CollectedSpecState {
    object Suspense : CollectedSpecState()
    data class OrderSuccess(val orderWithEntities: OrderWithEntities) : CollectedSpecState()
    data class OrderError(val throwable: Throwable) : CollectedSpecState()
}