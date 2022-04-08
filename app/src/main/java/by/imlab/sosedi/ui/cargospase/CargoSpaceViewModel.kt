package by.imlab.sosedi.ui.cargospase

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.model.SpaceType
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.usecase.PrintCargoUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CargoSpaceViewModel(
    private val printCargoUseCase: PrintCargoUseCase,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private lateinit var order: OrderEntity

    private val _state =
        MutableLiveData<CargoSpaceState>().default(initialValue = CargoSpaceState.Suspense)
    val state = _state

    val printState = printCargoUseCase.printState

    fun fetchOrderById(orderId: Long) {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesById(id = orderId).catch {
                _state.set(CargoSpaceState.OrderError(it))
            }.collect {
                order = it.order
                _state.set(CargoSpaceState.OrderSuccess(it))
            }
        }
    }

    fun printCargoSpace(number: Int) {
        //printCargoUseCase.print(printType = PrintCargoUseCase.PrintType.SpaceLabel, type = SpaceType.CARGO, order = order, number = number)
        printCargoUseCase.print(type = SpaceType.CARGO, order = order, number = number)
    }

    fun resetState() {
        _state.set(CargoSpaceState.Suspense)
    }

    fun printRetry() = printCargoUseCase.printRetry()
}