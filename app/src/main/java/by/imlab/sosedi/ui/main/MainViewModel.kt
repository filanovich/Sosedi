package by.imlab.sosedi.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.model.OrderStatus
import by.imlab.data.model.Result
import by.imlab.data.network.model.Update
import by.imlab.data.repository.LoginRepository
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.NavigationHelper
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.login.LoginState
import by.imlab.sosedi.ui.orderslist.OrdersListState
import by.imlab.sosedi.ui.specification.SpecificationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.scope.compat.ScopeCompat
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class MainViewModel(
    private val scannerHelper: ScannerHelper,
    private val navigationHelper: NavigationHelper,
    private val orderRepository: OrderRepository,
    private val loginRepository: LoginRepository,
    ) : ViewModel() {

    private var listener: ((result: Boolean) -> Unit)? = null
    fun setOnNotificationListener(listener: (result: Boolean) -> Unit) {
        this.listener = listener
    }

    private val _state =
        MutableLiveData<NotificationState>().default(initialValue = NotificationState.Suspense)
    val state get() = _state

    fun updateLastClickedItem(itemId: Int) = navigationHelper.updateLastClickedItem(itemId)

    val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //var a : ActionMenuItemView = toolbar.findViewById(R.id.refreshButton)
            //a?.callOnClick()
            val barcode = "123"
        }
    }

    val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val barcode = intent.getStringExtra(ScannerHelper.SCAN_BARCODE)
            barcode?.let { scannerHelper.updateLastBarcode(barcode = it) }
        }
    }

    fun updateLastBarcode(barcode: String) {
        barcode?.let { scannerHelper.updateLastBarcode(barcode = it) }
    }

    fun fetchNotification() {

        viewModelScope.launch(Dispatchers.IO) {

            while (true) {

                delay(60000)

                if (!loginRepository.isLoggedIn)
                    continue

                var hasOrders = false
                val orders = orderRepository.fetchOrdersList()
                if (orders.isNotEmpty())
                    hasOrders = orders.any { it.status == OrderStatus.UNDERWAY || it.status == OrderStatus.QUEUE }

                if (!hasOrders) {
                    orderRepository.checkNewOrders() {
                        listener?.invoke(it)
                    }
                } else
                    listener?.invoke(false)
            }
        }
    }

}