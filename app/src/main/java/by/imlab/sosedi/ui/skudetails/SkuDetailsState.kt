package by.imlab.sosedi.ui.skudetails

import by.imlab.data.database.model.SkuWithEntities
import by.imlab.data.model.BarcodeType

sealed class SkuDetailsState {
    object Suspense : SkuDetailsState()
    data class SkuSuccess(val skuWithEntities: SkuWithEntities) : SkuDetailsState()
    data class SkuError(val throwable: Throwable) : SkuDetailsState()
    data class ScanCommonSuccess(val currentValue: Int) : SkuDetailsState()
    data class ScanCustomSuccess(val currentValue: String) : SkuDetailsState()
    object ScanError : SkuDetailsState()
    data class ExcessError(val barcodeType: BarcodeType) : SkuDetailsState()
    object CollectionSuccess : SkuDetailsState()
    data class NotEnoughError(val barcodeType: BarcodeType) : SkuDetailsState()
}