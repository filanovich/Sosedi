package by.imlab.sosedi.ui.transferredorders

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.data.database.entity.OrderEntity
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.setDivider
import by.imlab.sosedi.ui.global.extentions.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.transferred_orders_fragment.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferredOrdersFragment : Fragment(R.layout.transferred_orders_fragment) {

    private val viewModel: TransferredOrdersViewModel by viewModel()
    private val adapter by lazy { TransferredOrdersAdapter() }

    //@InternalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set divider for transferred orders list
        transferredOrders.setDivider(R.drawable.recycler_view_divider)

        // Setup TransferredOrdersAdapter
        transferredOrders.adapter = adapter

        // Fetch transferred orders
        viewModel.fetchTransferredOrdersList()

        // Show logout dialog when clicked on back press button
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //dialog(R.string.you_have_pending_orders, R.string.complete, {
                    //findNavController().navigate(R.id.loginFragment)
                //}, R.string.cancel, {})
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Listen scanned barcode changes
        viewModel.fetchScannedBarcode()

        // Check fetching transferred orders
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state = state) }
    }

    private fun render(state: TransferredOrdersOrdersState) {
        when (state) {
            is TransferredOrdersOrdersState.OrdersListSuccess -> renderSuccess(orderList = state.orderList)
            is TransferredOrdersOrdersState.OrdersListError -> renderError()
            is TransferredOrdersOrdersState.ScanSuccess -> renderScanSuccess(orderNumber = state.orderNumber)
            is TransferredOrdersOrdersState.ScanError -> renderScanError()
        }
    }

    private fun renderSuccess(orderList: List<OrderEntity>) {
        // Add transferred orders count to title
        // TODO: Change this shit
        val title = requireActivity().toolbar.title
        val count = requireContext().getString(R.string.number_things, orderList.size.toString())
        if (!title.contains(count)) {
            requireActivity().toolbar.title = "$title   $count"
        }

        // Add transferred orders to adapter
        adapter.addOrders(orders = orderList)

        // Listen search changes
        // TODO: Change this to paging or something normal
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredOrders = orderList.filter {
                    it.number.contains(newText ?: "")
                }
                adapter.addOrders(orders = filteredOrders)
                return true
            }

        })

        // Set dots menu click listener
        adapter.setOnPopupMenuClickListener { orderId ->
            val action = TransferredOrdersFragmentDirections
                .actionTransferredOrdersFragmentToTransferredSpecFragment(orderId = orderId)
            findNavController().navigate(action)
            viewModel.resetState()
        }
    }

    private fun renderScanError() {
        toast(R.string.no_order_with_this_code)
    }

    private fun renderScanSuccess(orderNumber: String) {
        search.setQuery(orderNumber, true)
    }

    private fun renderError() {

    }


}