package by.imlab.sosedi.ui.transferredspec

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.repository.OrderRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TransferredSpecViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _state =
        MutableLiveData<TransferredSpecState>().default(initialValue = TransferredSpecState.Suspense)
    val state = _state

    fun fetchOrderById(id: Long) {
        viewModelScope.launch {
            orderRepository.fetchOrderWithEntitiesById(id = id).catch {
                _state.set(TransferredSpecState.OrderError(it))
            }.collect {
                _state.set(TransferredSpecState.OrderSuccess(orderWithEntities = it))
            }
        }
    }
}