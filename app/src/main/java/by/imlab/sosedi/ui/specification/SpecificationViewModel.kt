package by.imlab.sosedi.ui.specification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.api.model.StockBalanceResponse
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.OrderStatus
import by.imlab.data.model.Result
import by.imlab.data.repository.BarcodeRepository
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.PrintState
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.orderslist.OrdersListState
import by.imlab.sosedi.ui.usecase.PrintSpecUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class SpecificationViewModel(
    private val printSpecUseCase: PrintSpecUseCase,
    private val barcodeRepository: BarcodeRepository,
    private val orderRepository: OrderRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private var lastScannedDate = Date()

    private lateinit var _order: OrderWithEntities
    val orderWithEntities get() = _order
    val order get() = _order.order

    private val _state =
        MutableLiveData<SpecificationState>().default(initialValue = SpecificationState.Suspense)
    val state = _state

    val printState = printSpecUseCase.printState

    fun fetchScannedBarcode() {
        scannerHelper.lastBarcode.value?.scannedAt?.let {
            lastScannedDate = it
        }
        viewModelScope.launch {
            scannerHelper.lastBarcode.asFlow().collect { barcode ->
                if (checkNeedValidate(barcode)) {
                    validateBarcode(barcode = barcode)
                    lastScannedDate = barcode.scannedAt
                }
            }
        }
    }

    private fun checkNeedValidate(barcode: PrinterBarcode): Boolean {
        return this::_order.isInitialized
                && _state.value != SpecificationState.Suspense
                && !listOf("4813626001698", "4640026550347").contains(barcode.value)
                && lastScannedDate != barcode.scannedAt
    }

    fun validateBarcode(barcode: PrinterBarcode) {
        val barcodeEntity = BarcodeEntity(value = barcode.value)
        val isNotInOrder = !_order.checkBarcodeInOrder(barcodeEntity)
        when {
            isNotInOrder -> {
                _state.set(SpecificationState.ScanError)
            }
            else -> {
                val skuId = _order.skuList.find {
                    it.sku.checkBarcodeCorrect(barcodeEntity)
                }?.sku?.id
                val orderNumber = _order.order.number
                if (skuId != null) {
                    _state.set(
                        SpecificationState.ScanSuccess(
                            skuId = skuId,
                            orderNumber = orderNumber,
                            barcode = barcode.value,
                            allowedSpaces = _order.getAllowedSpacesNumber()
                        )
                    )
                }
            }
        }
    }

    fun fetchOrderById(id: Long) {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesById(id = id).catch {
                _state.set(SpecificationState.OrderError(it))
            }.collect {
                if (it != null) {
                    _order = it
                    _state.set(SpecificationState.OrderSuccess(orderWithEntities = _order))
                }
            }
        }
    }

    fun resetScannedCodes(skuId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            barcodeRepository.resetScannedCodes(skuId = skuId)
        }
    }

    fun validateOrderCollection(skipValidation: Boolean) {
        if (_order.isCollected() || skipValidation) {
            // TODO: Add printing label
            makeOrderCollected()
        } else {
            _state.set(SpecificationState.NotEnoughError)
        }
    }

    private fun makeOrderCollected() {

        val updatedOrder = _order.order.copy(status = OrderStatus.COLLECTION, collectedAt = Date())

        orderRepository.updateOrder(updatedOrder) { result ->
            when (result) {
                is Result.Success<*> -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        orderRepository.updateOrder(order = updatedOrder)
                    }
                    _state.set(SpecificationState.CollectionSuccess(order = updatedOrder))
                }
                is Result.Error -> {
                    _state.set(SpecificationState.CollectionError(message = result.message))
                }
            }
        }
    }

    fun getStockBalance(sku: SkuEntity) {

        orderRepository.getStockBalance(sku) { result ->
            when (result) {
                is Result.Success<*> -> {
                    _state.set(SpecificationState.StockBalanceSuccess(value = result.data as String))
                }
                is Result.Error -> {
                    _state.set(SpecificationState.StockBalanceError(message = result.message))
                }
            }
        }
    }

    fun getImage(sku: SkuEntity) {

        orderRepository.getImage(sku) { result ->
            when (result) {
                is Result.Success<*> -> {
                    _state.set(SpecificationState.ImageSuccess(image = result.data as String))
                }
                is Result.Error -> {
                    _state.set(SpecificationState.ImageError(message = result.message))
                }
            }
        }
    }

    fun printSpecificationLabel() {
        printSpecUseCase.print(orderWithEntities = _order)
    }

    fun resetState() {
        _state.set(SpecificationState.Suspense)
        printState.set(PrintState.Suspense)
    }

    fun printRetry() = printSpecUseCase.printRetry()
}