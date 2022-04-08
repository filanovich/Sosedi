package by.imlab.sosedi.ui.orderslist

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.SpaceType
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.OrdersListFragmentBinding
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.checkNetworkState
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.toast
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.collected_orders_fragment.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class OrdersListFragment : BaseFragment(R.layout.orders_list_fragment) {

    private val viewModel: OrdersListViewModel by viewModel()

    private val adapter: OrdersListAdapter by lazy { OrdersListAdapter() }

    private var _binding: OrdersListFragmentBinding? = null
    private val binding get() = _binding!!

    private var spotsDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = OrdersListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    //@InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        spotsDialog = SpotsDialog.Builder()
            .setCancelable(false)
            .setContext(requireContext()).build()

        adapter.setOnAcceptOrder { order ->
            if (requireContext().checkNetworkState()) {
                viewModel.acceptOrder(order)
            } else {
                val title = getString(R.string.no_internet_connection)
                val message = getString(
                    R.string.order_cannot_be_taken_to_work,
                    order.number
                )
                dialog(
                    title = title,
                    message = message,
                    positiveText = getString(R.string.to_order)
                ) {}
            }
        }

        // Setup SearchPrinterAdapter
        binding.orders.adapter = adapter

        // Check fetch orders list state
        viewModel.state.observe(viewLifecycleOwner) {
                state -> render(state = state)
        }

        // Check printing state
        viewModel.printState.observe(viewLifecycleOwner) { state ->
            renderPrintState(state = state)
        }

        // Listen scanned barcode changes
        viewModel.fetchScannedBarcode()

        // Reset calculated values
        resetCalculatedOrdersInfo()

        // Show logout dialog when clicked on back press button
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //dialog(R.string.you_have_pending_orders, R.string.complete, {
                    //findNavController().navigate(R.id.loginFragment)
                //}, R.string.cancel, {})
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Toolbar button listener
        lifecycleScope.launch {
            viewModel.lastClickedItem.asFlow().collect { itemId ->
                if (itemId == R.id.refreshButton) {
                    refreshOrdersList()
                }
            }
        }

        // Order accepted listener
/*        binding.orderMainItem.setOnAcceptListener {
            if (requireContext().checkNetworkState()) {
                viewModel.acceptMainOrder()
            } else {
                val title = getString(R.string.no_internet_connection)
                val message = getString(
                    R.string.order_cannot_be_taken_to_work,
                    viewModel.getMainOrderNumber()
                )
                dialog(
                    title = title,
                    message = message,
                    positiveText = getString(R.string.to_order)
                ) {}
            }
        }
*/

        refreshOrdersList()

        //MainActivity.mainActivity?.onNotifyNewOrders()
    }

    private fun render(state: OrdersListState) {
        when (state) {
            is OrdersListState.Suspense -> renderSuspense()
            is OrdersListState.OrdersListSuccess -> renderSuccess(
                orderList = state.orderList,
                hasUnderway = state.hasUnderway
            )
            is OrdersListState.OrdersListError -> renderError(state.message)
            is OrdersListState.OrderAcceptError -> renderError(state.message)
            is OrdersListState.OrderAccepted -> renderAccepted(order = state.order)
            is OrdersListState.OrdersListEmpty -> renderEmpty()

            is OrdersListState.ScanSuccess -> renderScanSuccess(orderNumber = state.orderNumber)
            is OrdersListState.ScanError -> renderScanError()
        }
    }

    private fun renderSuspense() {
        // Clear adapter list to be sure we don't have the showed orders
        adapter.clearList()

        // Hide error message when loafing the orders list
        binding.errorMessage.isVisible = false

        // Reset calculated values
        resetCalculatedOrdersInfo()
    }

    private fun renderSuccess(
        orderList: List<OrderWithEntities>,
        hasUnderway: Boolean
    ) {

        binding.hasUnderway.isVisible = false
        binding.searchLayout.isVisible = false

        if (hasUnderway)
            binding.hasUnderway.isVisible = true

        if (orderList.isEmpty())
            return;

        binding.searchLayout.isVisible = true

        adapter.addOrders(orderList, hasUnderway)

        // Listen search changes
        // TODO: Change this to paging or something normal
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredOrders = orderList.filter {
                    it.order.number.contains(newText ?: "")
                }
                adapter.addOrders(orders = filteredOrders, false)
                return true
            }

        })

        // Hide error message after success loading
        binding.errorMessage.isVisible = false

        // Calculate new orders values
        calculateOrdersInfo(ordersList = orderList)
    }

    private fun renderError(message: String?) {
        // Show internet error message
        // TODO: Add more error messages
        if (message != null) {
            dialog(
                message = message,
                positiveText = getString(R.string.ok)
            ) {}
        }

        //toast(message)

        //binding.errorMessage.isVisible = true

        // Reset calculated values
        resetCalculatedOrdersInfo()
    }

    private fun renderEmpty() {
/*        binding.orderMainItem.hideAllButtons() */

        // Reset calculated values
        resetCalculatedOrdersInfo()
    }

    private fun renderAccepted(order: OrderEntity) {
        val currentDate: String =
            SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date())
        val currentTime =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val message = getString(
            R.string.order_accepted_in,
            order.number,
            currentDate,
            currentTime
        )
        dialog(message = message, positiveText = getString(R.string.to_order)) {
            viewModel.printCargoSpace(SpaceType.CARGO, order, 1)
            //val action =
            //    OrdersListFragmentDirections.actionOrdersListFragmentToOpenPackageFragment()
            //findNavController().navigate(action)
        }
    }

    private fun refreshOrdersList() {

        spotsDialog?.show()

        lifecycleScope.launch {
/*            binding.orderMainItem.setOrder(null) */
            resetCalculatedOrdersInfo()
            adapter.clearList()
            viewModel.getOrders() {
                spotsDialog?.dismiss()
            }
        }
    }

    private fun renderScanError() {
        toast(R.string.no_order_with_this_code)
    }

    private fun renderScanSuccess(orderNumber: String) {
        search.setQuery(orderNumber, true)
    }

    private fun calculateOrdersInfo(ordersList: List<OrderWithEntities>) {
        val total = ordersList.size
        binding.totalNewOrdersCount.text = getString(R.string.number_things, total.toString())

        // Calculate total assembly time
        val totalAssembly = ordersList.sumOf { it.skuList.count() }

        val hours = getString(R.string.number_hours, (totalAssembly / 60).toString())
        val minutes = getString(R.string.number_minutes, (totalAssembly % 60).toString())
        binding.totalAssemblyTimeCount.text = if (totalAssembly > 60) {
            "$hours $minutes"
        } else {
            minutes
        }
    }

    private fun resetCalculatedOrdersInfo() {
        calculateOrdersInfo(ordersList = emptyList())
    }

    override fun onPrintSuccess() {
        //val action = OpenPackageFragmentDirections.actionOpenPackageFragmentToOrderDetailsFragment()
        val order = viewModel.mainOrder
        val action = OrdersListFragmentDirections
            .actionOrdersListFragmenToSpecificationFragment(
                orderId = order.id,
                canEdit = true
            )
        findNavController().navigate(action)
        viewModel.resetState()
    }

    override fun onPrintRetry() {
        viewModel.printRetry()
    }
}