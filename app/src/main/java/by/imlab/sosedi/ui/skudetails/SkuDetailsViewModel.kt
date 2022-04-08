package by.imlab.sosedi.ui.skudetails

import android.icu.text.DecimalFormat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.model.SkuWithEntities
import by.imlab.data.model.BarcodeType
import by.imlab.data.model.SpaceType
import by.imlab.data.repository.BarcodeRepository
import by.imlab.data.repository.SkuRepository
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.usecase.PrintCargoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.abs

class SkuDetailsViewModel(
    private val printCargoUseCase: PrintCargoUseCase,
    private val barcodeRepository: BarcodeRepository,
    private val skuRepository: SkuRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private lateinit var skuWithEntities: SkuWithEntities

    val currentSku get() = skuWithEntities.sku

    // Order number
    private var orderNumber = ""
    private var orderAddress = ""

    // Scanned codes
    val scannedCodes get() = skuWithEntities.scannedCodes

    // Barcodes
    private val barcodeType get() = currentSku.barcodeType

    // Scanned barcode
    private lateinit var scannedBarcode: BarcodeEntity

    // Cargo space
    private var useCargoSpaces = false
    private var cargoSpacesCount = 1

    // Sku details state
    private val _state =
        MutableLiveData<SkuDetailsState>().default(initialValue = SkuDetailsState.Suspense)
    val state = _state

    val printState = printCargoUseCase.printState

    fun fetchScannedBarcode() {
        viewModelScope.launch {
            scannerHelper.lastBarcode.asFlow().collect { barcode ->
                if (checkNeedValidate()) validateBarcode(barcode = barcode)
            }
        }
    }

    private fun checkNeedValidate(): Boolean {
        return this::skuWithEntities.isInitialized && _state.value != SkuDetailsState.Suspense
    }

    fun validateBarcode(barcode: PrinterBarcode) {
        scannedBarcode = BarcodeEntity(skuId = currentSku.id, value = barcode.value)
        if (barcode.value == "4813626001698" || barcode.value == "4640026550347") {
            cargoSpacesCount = cargoSpacesCount.plus(1)
        } else if (currentSku.checkBarcodeCorrect(scannedBarcode)) {
            checkQuantityOrWeight()
        } else {
            _state.set(SkuDetailsState.ScanError)
        }
    }

    fun validateSkuCollection(skipValidation: Boolean) {
        if (skuWithEntities.isCollected() || skipValidation || currentSku.barcodeType == BarcodeType.CustomWeight) {
            if (useCargoSpaces) {
                printCargoUseCase.print(
                    type = SpaceType.CARGO,
                    orderId = currentSku.orderId,
                    orderNumber = orderNumber,
                    number = cargoSpacesCount
                )
            } else {
                updateSkuAndContinue()
            }
        } else {
            _state.set(SkuDetailsState.NotEnoughError(barcodeType = barcodeType))
        }
    }

    fun fetchSkuById(id: Long, orderNumber: String) {
        this.orderNumber = orderNumber
        viewModelScope.launch {
            skuRepository.fetchSkuWithEntitiesById(id = id).catch {
                _state.set(SkuDetailsState.SkuError(it))
            }.collect {
                if (it != null) {
                    skuWithEntities = it
                    _state.set(SkuDetailsState.SkuSuccess(skuWithEntities = it))
                } else {
                    _state.set(SkuDetailsState.CollectionSuccess)
                }
            }
        }
    }

    fun updateScannedList(newList: MutableList<BarcodeEntity>) {
        scannedCodes.clear()
        scannedCodes.addAll(newList)
        _state.set(SkuDetailsState.ScanCustomSuccess(currentValue = formatCurrentValue()))
    }

    private fun checkQuantityOrWeight() {
        when (barcodeType) {
            BarcodeType.CommonGoods -> checkCommonQuantity()
            else -> checkCustomQuantity()
        }
    }

    private fun checkCommonQuantity() {
        val maxValue = currentSku.quantity
        val newValue = scannedBarcode.getQuantity()
        val currentValue = scannedCodes.count()
        if ((newValue + currentValue) <= maxValue) {
            // Add code to the list
            scannedCodes.add(scannedBarcode)

            // Count to string and send next
            _state.set(SkuDetailsState.ScanCommonSuccess(currentValue = scannedCodes.size))
        }
    }

    private fun checkCustomQuantity() {
        val requiredQuality = currentSku.quantity
        val currentQuality = scannedBarcode.getQuantity()
        val scannedQuality = scannedCodes.map { it.getQuantity() }.sum()

        // Current and scanned qualities
        val currAndScan = (currentQuality + scannedQuality)

        // Permissible error in quality not more than 10% in both directions
        //val possibleQuality = abs(100 * (requiredQuality - currAndScan) / currAndScan) < 10
        //if (currAndScan <= requiredQuality || possibleQuality) {
            // Add code to the list
            scannedCodes.add(scannedBarcode)

            // Count to string and send next
            _state.set(SkuDetailsState.ScanCustomSuccess(currentValue = formatCurrentValue()))
        //} else {
        //    _state.set(SkuDetailsState.ExcessError(barcodeType = barcodeType))
        //}
    }

    private fun formatCurrentValue(): String {
        return when (barcodeType) {
            BarcodeType.CustomGoods -> {
                scannedCodes.map { it.getQuantity() }.sum().toInt().toString()
            }
            BarcodeType.CustomWeight -> {
                val newCurrentValue = scannedCodes.map { it.getQuantity() }.sum()
                DecimalFormat("#0.000").format(newCurrentValue)
            }
            else -> "-"
        }
    }

    fun updateSkuAndContinue() {
        viewModelScope.launch(Dispatchers.IO) {
            barcodeRepository.updateScannedCodes(skuId = currentSku.id, scannedCodes = scannedCodes)
            if (scannedCodes.size == 0) {
                skuRepository.moveSkuToLastPosition(currentSku)
            }
            _state.set(SkuDetailsState.CollectionSuccess)
        }
    }

    fun updateSpacesNumber(value: Int) {
        cargoSpacesCount = value
    }

    fun useCargoSpaces(use: Boolean) {
        useCargoSpaces = use
    }

    fun addCommonScannedBarcode() {
        if (this::scannedBarcode.isInitialized) {
            scannedCodes.add(scannedBarcode)
        } else {
            validateBarcode(PrinterBarcode(value = currentSku.barcodeId))
        }
    }

    @ExperimentalStdlibApi
    fun removeCommonScannedBarcode() {
        scannedCodes.removeLast()
    }

    fun resetState() {
        _state.set(SkuDetailsState.Suspense)

    }

    fun printRetry() = printCargoUseCase.printRetry()
}