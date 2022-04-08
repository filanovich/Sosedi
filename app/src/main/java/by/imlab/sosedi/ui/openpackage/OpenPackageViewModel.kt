package by.imlab.sosedi.ui.openpackage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.OrderStatus
import by.imlab.data.model.SpaceType
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.PrintState
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.usecase.PrintCargoUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OpenPackageViewModel(
    private val printCargoUseCase: PrintCargoUseCase,
    private val orderRepository: OrderRepository,
    private val scannerHelper: ScannerHelper
) : ViewModel() {

    private lateinit var _order: OrderWithEntities
    val order get() = _order.order

    private val _state =
        MutableLiveData<OpenPackageState>().default(initialValue = OpenPackageState.Suspense)
    val state = _state

    val printState = printCargoUseCase.printState

    fun fetchUnderwayOrder() {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesByStatus(status = OrderStatus.UNDERWAY).catch {
                _state.set(OpenPackageState.OrderError(it))
            }.collect {
                if (it.isEmpty() && !this@OpenPackageViewModel::_order.isInitialized) {
                    _state.set(OpenPackageState.OrderError(Throwable()))
                } else if (it.isNotEmpty()) {
                    _order = it.first()
                    _state.set(OpenPackageState.OrderSuccess(_order))
                }
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
        return this::_order.isInitialized && _state.value != OpenPackageState.Suspense
    }

    private fun validateBarcode(barcode: PrinterBarcode) {
        if (barcode.value == "4813626001698" || barcode.value == "4640026550347") {
            printCargoSpace()
        } else {
            _state.set(OpenPackageState.WrongCodeError)
        }
    }

    fun printCargoSpace(type: SpaceType = SpaceType.POCKET, number: Int = 1) {
        printCargoUseCase.print(type = type, order = _order.order, number = number)
    }

    fun resetState() {
        _state.set(OpenPackageState.Suspense)
        printState.set(PrintState.Suspense)
    }

    fun printRetry() = printCargoUseCase.printRetry()
}