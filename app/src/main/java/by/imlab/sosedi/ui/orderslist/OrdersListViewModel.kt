package by.imlab.sosedi.ui.orderslist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.OrderStatus
import by.imlab.data.model.Result
import by.imlab.data.model.SpaceType
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.NavigationHelper
import by.imlab.sosedi.ui.global.helpers.PrintState
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.usecase.PrintCargoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class OrdersListViewModel(
    private val printCargoUseCase: PrintCargoUseCase,
    private val orderRepository: OrderRepository,
    private val scannerHelper: ScannerHelper,
    navigationHelper: NavigationHelper
) : ViewModel() {

    private var lastScannedDate = Date()
    private lateinit var _orderList: List<OrderWithEntities>

    private val _state =
        MutableLiveData<OrdersListState>().default(initialValue = OrdersListState.Suspense)
    val state get() = _state

    fun fetchScannedBarcode() {
        scannerHelper.lastBarcode.value?.scannedAt?.let {
            lastScannedDate = it
        }
        viewModelScope.launch {
            scannerHelper.lastBarcode.asFlow().collect { barcode ->
                if (checkNeedValidate(barcode)) validateBarcode(barcode = barcode)
            }
        }
    }

    private fun checkNeedValidate(barcode: PrinterBarcode): Boolean {
        return this::_orderList.isInitialized &&
                _state.value != OrdersListState.Suspense &&
                lastScannedDate != barcode.scannedAt
    }

    private fun validateBarcode(barcode: PrinterBarcode) {
        val barcodeEntity = BarcodeEntity(value = barcode.value)
        val scannedOrder = _orderList.find { barcodeEntity.value.contains(it.order.number) }
        if (scannedOrder != null) {
            _state.set(OrdersListState.ScanSuccess(orderNumber = scannedOrder.order.number))
        } else {
            _state.set(OrdersListState.ScanError)
        }
    }

    val printState = printCargoUseCase.printState

    private lateinit var _mainOrder: OrderEntity
    val mainOrder get() = _mainOrder

    val lastClickedItem = navigationHelper.lastClickedItem

    fun fetchNewOrdersList(callback: () -> Unit) {

        viewModelScope.launch {
            orderRepository.fetchOrdersListWithEntities().catch {
                _state.set(OrdersListState.OrdersListError(it.message))
                callback.invoke()
            }.collect { orders ->
                if (orders.isNotEmpty()) {
                    val hasUnderway = orders.any { it.order.status == OrderStatus.UNDERWAY }
                    val queueOrders = orders.filter { it.order.status == OrderStatus.QUEUE }

                    _orderList = queueOrders
//                    if (queueOrders.isNotEmpty())
//                        _mainOrder = queueOrders.first().order

                    _state.set(
                        OrdersListState.OrdersListSuccess(
                            orderList = queueOrders,
                            hasUnderway = hasUnderway
                        )
                    )
                } else {
                    _state.set(OrdersListState.OrdersListEmpty)
                }
                callback.invoke()
            }
        }
    }

    fun getOrders(callback: () -> Unit) {

        orderRepository.getOrders() {
            if (it == null) {
                fetchNewOrdersList() {
                    callback.invoke()
                }
            } else {
                _state.setValue(OrdersListState.OrdersListError(it))
                callback.invoke()
            }
        }
    }

    fun acceptOrder(order: OrderEntity) {

        val _order = order.copy(status = OrderStatus.UNDERWAY)

        orderRepository.updateOrder(_order) { result ->
            when (result) {
                is Result.Success<*> -> {
                    // Update order state to underway
                    viewModelScope.launch(Dispatchers.IO) {
                        orderRepository.updateOrder(order = _order)
                    }
                    _state.set(OrdersListState.OrderAccepted(_order))
                }
                is Result.Error -> {
                    _state.set(OrdersListState.OrderAcceptError(result.message))
                }
            }
        }
    }

    fun printCargoSpace(type: SpaceType = SpaceType.POCKET, order: OrderEntity, number: Int = 1) {
        _mainOrder = order
        printCargoUseCase.print(type = type, order = order, number = number)
    }

    fun resetState() {
        _state.set(OrdersListState.Suspense)
        printState.set(PrintState.Suspense)
    }

    fun printRetry() = printCargoUseCase.printRetry()
}