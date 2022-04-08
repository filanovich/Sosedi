package by.imlab.sosedi.ui.labelprint

import by.imlab.data.database.entity.CargoSpaceEntity

sealed class LabelPrintState {
    object Suspense : LabelPrintState()
    data class OrderSuccess(val cargoSpaces: List<CargoSpaceEntity>, val addSpacesAllow: Boolean) :
        LabelPrintState()

    data class OrderError(val throwable: Throwable) : LabelPrintState()
}