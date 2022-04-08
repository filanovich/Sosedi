package by.imlab.sosedi.ui.labelprint

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.usecase.PrintCargoUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LabelPrintViewModel(
    private val orderRepository: OrderRepository,
    private val printCargoUseCase: PrintCargoUseCase
) : ViewModel() {

    private lateinit var mainOrder: OrderEntity

    private val _state =
        MutableLiveData<LabelPrintState>().default(initialValue = LabelPrintState.Suspense)
    val state get() = _state

    val printState = printCargoUseCase.printState

    fun fetchOrderById(orderId: Long) {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesById(id = orderId).catch {
                _state.set(LabelPrintState.OrderError(it))
            }.collect {
                mainOrder = it.order
                _state.set(
                    LabelPrintState.OrderSuccess(
                        cargoSpaces = it.cargoSpaceList,
                        addSpacesAllow = it.isAddSpacesAllow()
                    )
                )
            }
        }
    }

    fun printRetry() = printCargoUseCase.printRetry()

    fun printCargoSpace(cargo: CargoSpaceEntity) {
        printCargoUseCase.print(orderNumber = mainOrder.number, cargo = cargo)
    }

    fun resetState() {
        _state.set(LabelPrintState.Suspense)
    }
}