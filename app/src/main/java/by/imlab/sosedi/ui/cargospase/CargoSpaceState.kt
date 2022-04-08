package by.imlab.sosedi.ui.cargospase

import by.imlab.data.database.model.OrderWithEntities

sealed class CargoSpaceState {
    object Suspense : CargoSpaceState()
    data class OrderSuccess(val orderWithEntities: OrderWithEntities) : CargoSpaceState()
    data class OrderError(val throwable: Throwable) : CargoSpaceState()
    object WrongCodeError : CargoSpaceState()
}