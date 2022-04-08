package by.imlab.sosedi.ui.transferredorders

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

class TransferredOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private lateinit var _orderList: List<OrderEntity>

    private val _state =
        MutableLiveData<TransferredOrdersOrdersState>().default(initialValue = TransferredOrdersOrdersState.Suspense)
    val state get() = _state

    fun fetchScannedBarcode() {
        viewModelScope.launch {
            scannerHelper.lastBarcode.asFlow().collect { barcode ->
                if (checkNeedValidate()) validateBarcode(barcode = barcode)
            }
        }
    }

    private fun checkNeedValidate(): Boolean {
        return this::_orderList.isInitialized && _state.value != TransferredOrdersOrdersState.Suspense
    }

    private fun validateBarcode(barcode: PrinterBarcode) {
        val barcodeEntity = BarcodeEntity(value = barcode.value)
        val scannedOrder = _orderList.find { barcodeEntity.value.contains(it.number) }
        if (scannedOrder != null) {
            _state.set(TransferredOrdersOrdersState.ScanSuccess(orderNumber = scannedOrder.number))
        } else {
            _state.set(TransferredOrdersOrdersState.ScanError)
        }
    }

    fun fetchTransferredOrdersList() {
        viewModelScope.launch {
            orderRepository.fetchOrderByStatus(status = OrderStatus.TRANSFER).catch {
                _state.set(TransferredOrdersOrdersState.OrdersListError(it))
            }.collect { orderList ->
                if (orderList.isNotEmpty()) {
                    _orderList = orderList
                    _state.set(
                        TransferredOrdersOrdersState.OrdersListSuccess(orderList = orderList)
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.set(TransferredOrdersOrdersState.Suspense)
    }
}