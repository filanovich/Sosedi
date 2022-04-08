package by.imlab.sosedi.ui.collectedspec

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.usecase.PrintSpecUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CollectedSpecViewModel(
    private val printSpecUseCase: PrintSpecUseCase,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private lateinit var order: OrderWithEntities

    private val _state =
        MutableLiveData<CollectedSpecState>().default(initialValue = CollectedSpecState.Suspense)
    val state = _state

    val printState = printSpecUseCase.printState

    fun fetchOrderById(id: Long) {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesById(id = id).catch {
                _state.set(CollectedSpecState.OrderError(it))
            }.collect {
                order = it
                _state.set(CollectedSpecState.OrderSuccess(orderWithEntities = it))
            }
        }
    }

    fun printSpecificationLabel() {
        printSpecUseCase.print(orderWithEntities = order)
    }
}