package by.imlab.sosedi.ui.underway

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.model.OrderStatus
import by.imlab.data.repository.OrderRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UnderwayViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _state =
        MutableLiveData<UnderwayState>().default(initialValue = UnderwayState.Suspense)
    val state = _state

    fun fetchUnderwayOrder() {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesByStatus(status = OrderStatus.UNDERWAY).catch {
                _state.set(UnderwayState.OrderError(it))
            }.collect {
                if (it.isEmpty()) {
                    _state.set(UnderwayState.OrderError(Throwable()))
                } else {
                    _state.set(UnderwayState.OrderSuccess(orderWithEntities = it.first()))
                }
            }
        }
    }
}