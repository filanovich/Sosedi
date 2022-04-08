package by.imlab.sosedi.ui.cancelledorders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.model.OrderStatus
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class CancelledOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private var lastScannedDate = Date()
    private lateinit var _orderList: List<OrderEntity>

    private val _state =
        MutableLiveData<CancelledOrdersState>().default(initialValue = CancelledOrdersState.Suspense)
    val state get() = _state

    fun fetchScannedBarcode() {
        scannerHelper.lastBarcode.value?.scannedAt?.let {
            lastScannedDate = it
        }
        viewModelScope.launch {
            scannerHelper.lastBarcode.asFlow().collect { barcode ->
                if (checkNeedValidate(barcode))
                    validateBarcode(barcode = barcode)
            }
        }
    }

    private fun checkNeedValidate(barcode: PrinterBarcode): Boolean {
        return this::_orderList.isInitialized &&
                _state.value != CancelledOrdersState.Suspense &&
                lastScannedDate != barcode.scannedAt
    }

    private fun validateBarcode(barcode: PrinterBarcode) {
        val barcodeEntity = BarcodeEntity(value = barcode.value)
        val scannedOrder = _orderList.find { barcodeEntity.value.contains(it.number.replace("-", "")) }
        if (scannedOrder != null) {
            _state.set(CancelledOrdersState.ScanSuccess(orderNumber = scannedOrder.number))
        } else {
            _state.set(CancelledOrdersState.ScanError)
        }
    }

    fun fetchCancelledOrdersList() {
        viewModelScope.launch {
            orderRepository.fetchOrderByStatus(status = OrderStatus.CANCEL).catch {
                _state.set(CancelledOrdersState.OrdersListError(it))
            }.collect { orderList ->
                if (orderList.isNotEmpty()) {
                    _orderList = orderList
                    _state.set(
                        CancelledOrdersState.OrdersListSuccess(orderList = orderList)
                    )
                } else {
                    _state.set(CancelledOrdersState.OrdersListError(Throwable("Empty list")))
                }
            }
        }
    }

    fun resetState() {
        _state.set(CancelledOrdersState.Suspense)
    }
}