package by.imlab.sosedi.ui.collectedtransfer

import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import java.util.*

sealed class CollectedTransferState {
    object Suspense : CollectedTransferState()
    data class OrderSuccess(val orderWithEntities: OrderWithEntities) : CollectedTransferState()
    data class OrderError(val throwable: Throwable) : CollectedTransferState()
    data class ScanSuccess(val cargoSpaces: List<CargoSpaceEntity>) : CollectedTransferState()
    object ScanError : CollectedTransferState()
    data class TransferSuccess(
        val order: OrderEntity,
        val transferredAt: Date
    ) : CollectedTransferState()
    data class TransferError(val message: String?) : CollectedTransferState()

    object SpotsDialogOn : CollectedTransferState()
    object SpotsDialogOff : CollectedTransferState()
}