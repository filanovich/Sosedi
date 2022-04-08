package by.imlab.sosedi.ui.openpackage

import by.imlab.data.database.model.OrderWithEntities

sealed class OpenPackageState {
    object Suspense : OpenPackageState()
    data class OrderSuccess(val order: OrderWithEntities) : OpenPackageState()
    data class OrderError(val throwable: Throwable) : OpenPackageState()
    object WrongCodeError : OpenPackageState()
}