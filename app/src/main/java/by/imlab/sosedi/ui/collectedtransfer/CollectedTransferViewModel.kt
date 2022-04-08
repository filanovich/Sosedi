package by.imlab.sosedi.ui.collectedtransfer

import android.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.OrderStatus
import by.imlab.data.model.Result
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.login.LoginState
import by.imlab.sosedi.ui.orderslist.OrdersListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class CollectedTransferViewModel(
    private val orderRepository: OrderRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private var lastScannedDate = Date()

    private lateinit var mainOrder: OrderWithEntities
    private lateinit var cargoSpaces: List<CargoSpaceEntity>

    private val _state =
        MutableLiveData<CollectedTransferState>().default(initialValue = CollectedTransferState.Suspense)
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
        return this::mainOrder.isInitialized
                && _state.value != CollectedTransferState.Suspense
                && lastScannedDate != barcode.scannedAt
    }

    // TODO: Add validation
    private fun validateBarcode(barcode: PrinterBarcode) {
        val correctCargo = cargoSpaces.filter { it.barcode == barcode.value }
        if (correctCargo.isNotEmpty()) {
            val updatedList = cargoSpaces.map {
                if (it == correctCargo.first()) it.copy(scanned = true) else it
            }
            cargoSpaces = updatedList
            _state.set(CollectedTransferState.ScanSuccess(cargoSpaces))
        } else {
            _state.set(CollectedTransferState.ScanError)
        }
    }

    fun checkOrderDone() {
        val hasDone = cargoSpaces.all { it.scanned }
        if (hasDone) {

            _state.setValue(CollectedTransferState.SpotsDialogOn)

            val transferredDate = Date()
            val order = mainOrder.order.copy(
                status = OrderStatus.TRANSFER,
                transferredAt = transferredDate
            )

            orderRepository.updateOrder(order) { result ->

                _state.setValue(CollectedTransferState.SpotsDialogOff)

                when (result) {
                    is Result.Success<*> -> {

                        orderRepository.updateOrder(order = order)

                        _state.set(
                            CollectedTransferState.TransferSuccess(
                                order = mainOrder.order,
                                transferredAt = transferredDate
                            )
                        )
                    }
                    is Result.Error -> {
                        _state.set(CollectedTransferState.TransferError(message = result.message))
                    }
                }
            }
        } else {
            _state.set(CollectedTransferState.TransferError(null))
        }
    }

//    private fun updateOrderData(transferredDate: Date) {
//        viewModelScope.launch(Dispatchers.IO) {
//            orderRepository.updateOrder(
//                order = mainOrder.order.copy(
//                    status = OrderStatus.TRANSFER,
//                    transferredAt = transferredDate
//                )
//            )
//        }
//    }

    fun fetchOrderById(orderId: Long) {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesById(id = orderId).catch {
                _state.set(CollectedTransferState.OrderError(it))
            }.collect { order ->
                mainOrder = order
                cargoSpaces = mainOrder.cargoSpaceList
                _state.set(
                    CollectedTransferState.OrderSuccess(orderWithEntities = mainOrder)
                )
            }
        }
    }

    fun resetState() {
        _state.set(CollectedTransferState.Suspense)
    }
}