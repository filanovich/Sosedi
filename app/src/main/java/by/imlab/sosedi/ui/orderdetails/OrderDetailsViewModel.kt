package by.imlab.sosedi.ui.orderdetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.OrderStatus
import by.imlab.data.model.Result
import by.imlab.data.repository.OrderRepository
import by.imlab.data.repository.SkuRepository
import by.imlab.sosedi.ui.global.helpers.PrintState
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.usecase.PrintSpecUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class OrderDetailsViewModel(
    private val printSpecUseCase: PrintSpecUseCase,
    private val orderRepository: OrderRepository,
    private val skuRepository: SkuRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private var lastScannedDate = Date()

    private lateinit var _order: OrderWithEntities
    val order get() = _order.order

    private val _state =
        MutableLiveData<OrderDetailsState>().default(initialValue = OrderDetailsState.Suspense)
    val state = _state

    val printState = printSpecUseCase.printState

    fun fetchScannedBarcode() {
        scannerHelper.lastBarcode.value?.scannedAt?.let {
            lastScannedDate = it
        }
        viewModelScope.launch {
            scannerHelper.lastBarcode.asFlow().collect { barcode ->
                if (checkNeedValidate(barcode)) {
                    lastScannedDate = barcode.scannedAt
                    validateBarcode(barcode = barcode)
                }
            }
        }
    }

    fun printSpecificationLabel() {
        printSpecUseCase.print(orderWithEntities = _order)
    }

    private fun checkNeedValidate(barcode: PrinterBarcode): Boolean {
        return this::_order.isInitialized &&
                _state.value != OrderDetailsState.Suspense &&
                !listOf("4813626001698", "4640026550347").contains(barcode.value) &&
                lastScannedDate != barcode.scannedAt
    }

    // FIXME: Fix this
    fun validateBarcode(barcode: PrinterBarcode) {
        val barcodeEntity = BarcodeEntity(value = barcode.value)
        val isNotInOrder = !_order.checkBarcodeInOrder(barcodeEntity)
        val isOutOfTurn = !_order.checkBarcodeIsCurrent(barcodeEntity)
        when {
            isNotInOrder -> {
                _state.set(OrderDetailsState.ScanError(error = OrderDetailsError.NotInOrder))
            }
            isOutOfTurn -> {
                _state.set(OrderDetailsState.ScanError(error = OrderDetailsError.OutOfTurn))
            }
            else -> {
                val skuId = _order.getCurrentSku()?.sku?.id
                val orderNumber = _order.order.number
                val orderAddress = _order.order.address
                if (skuId != null) {
                    _state.set(
                        OrderDetailsState.ScanSuccess(
                            skuId = skuId,
                            orderNumber = orderNumber,
                            barcode = barcode.value,
                            allowedSpaces = _order.getAllowedSpacesNumber(),
                        )
                    )
                }
            }
        }
    }

    fun fetchCollectionOrder() {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesByStatus(status = OrderStatus.UNDERWAY).catch {
                _state.set(OrderDetailsState.OrderError(it))
            }.collect {
                if (it.isEmpty()) {
                    if (_order.isCollected()) {
                        makeOrderCollected()
                    } else {
                        _state.set(OrderDetailsState.OrderError(Throwable()))
                    }
                } else {
                    _order = it.first()
                    if (_order.isCollected()) {
                        makeOrderCollected()
                    } else {
                        _state.set(OrderDetailsState.OrderSuccess(orderWithEntities = _order))
                    }
                }
            }
        }
    }

    private fun makeOrderCollected() {

        val order = _order.order.copy(
            status = OrderStatus.COLLECTION,
            transferredAt = Date()
        )

        orderRepository.updateOrder(order) { result ->
            when (result) {
                is Result.Success<*> -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        orderRepository.updateOrder(order = order)
                    }
                    _state.set(OrderDetailsState.CollectionSuccess(order = order))
                }
                is Result.Error -> {
                    _state.set(OrderDetailsState.CollectionError(message = result.message))
                }
            }
        }
    }

    // Add scanned list migration after changing sku id
    fun currentSkuToLastPosition() {
        _order.getCurrentSku()?.sku?.let { sku ->
            viewModelScope.launch(Dispatchers.IO) {
                skuRepository.moveSkuToLastPosition(sku = sku)
            }
        }
    }

    fun resetState() {
        _state.set(OrderDetailsState.Suspense)
        printState.set(PrintState.Suspense)
    }

    fun makeOrdering() {
        _state.set(OrderDetailsState.Ordering)
        printState.set(PrintState.Suspense)
    }

    fun printRetry() = printSpecUseCase.printRetry()
}