package by.imlab.sosedi.ui.openpocket

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.model.SpaceType
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.usecase.PrintCargoUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OpenPocketViewModel(
    private val printCargoUseCase: PrintCargoUseCase,
    private val orderRepository: OrderRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private lateinit var order: OrderEntity

    private val _state =
        MutableLiveData<OpenPocketState>().default(initialValue = OpenPocketState.Suspense)
    val state = _state

    val printState = printCargoUseCase.printState

    fun fetchOrderById(orderId: Long) {
        viewModelScope.launch {
            orderRepository.fetchOrderById(id = orderId).catch {
                _state.set(OpenPocketState.OrderError(it))
            }.collect {
                order = it
            }
        }
    }

    fun fetchScannedBarcode() {
        viewModelScope.launch {
            scannerHelper.lastBarcode.asFlow().collect { barcode ->
                if (checkNeedValidate()) validateBarcode(barcode = barcode)
            }
        }
    }

    private fun checkNeedValidate(): Boolean {
        return this::order.isInitialized
    }

    private fun validateBarcode(barcode: PrinterBarcode) {
        if (barcode.value == "4813626001698" || barcode.value == "4640026550347") {
            printCargoSpace()
        } else {
            _state.set(OpenPocketState.WrongCodeError)
        }
    }

    private fun printCargoSpace() {
        printCargoUseCase.print(type = SpaceType.POCKET, order = order)
    }

    fun resetState() {
        _state.set(OpenPocketState.Suspense)
    }

    fun printRetry() = printCargoUseCase.printRetry()
}