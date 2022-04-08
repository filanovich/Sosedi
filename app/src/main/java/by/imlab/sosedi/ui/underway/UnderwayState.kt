package by.imlab.sosedi.ui.underway

import by.imlab.data.database.model.OrderWithEntities

sealed class UnderwayState {
    object Suspense : UnderwayState()
    data class OrderSuccess(val orderWithEntities: OrderWithEntities) : UnderwayState()
    data class OrderError(val throwable: Throwable) : UnderwayState()
}