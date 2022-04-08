package by.imlab.sosedi.ui.collectedorders

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.data.database.entity.OrderEntity
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.setDivider
import by.imlab.sosedi.ui.global.extentions.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.collected_orders_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollectedOrdersFragment : Fragment(R.layout.collected_orders_fragment) {

    private val viewModel: CollectedOrdersViewModel by viewModel()
    private val adapter by lazy { CollectedOrdersAdapter() }

    //@InternalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set divider for collected orders list
        cargoSpaces.setDivider(R.drawable.recycler_view_divider)

        // Setup SearchPrinterAdapter
        cargoSpaces.adapter = adapter

        // Show logout dialog when clicked on back press button
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //dialog(R.string.you_have_pending_orders, R.string.complete, {
                    //findNavController().navigate(R.id.loginFragment)
                //}, R.string.cancel, {})
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Fetch collected orders
        viewModel.fetchCollectedOrdersList()

        // Listen scanned barcode changes
        viewModel.fetchScannedBarcode()

        // Check fetching collected orders
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state = state) }
    }

    private fun render(state: CollectedOrdersState) {
        when (state) {
            is CollectedOrdersState.OrdersListSuccess -> renderSuccess(orderList = state.orderList)
            is CollectedOrdersState.OrdersListError -> renderError()
            is CollectedOrdersState.ScanSuccess -> renderScanSuccess(orderNumber = state.orderNumber)
            is CollectedOrdersState.ScanError -> renderScanError()
        }
    }

    private fun renderSuccess(orderList: List<OrderEntity>) {
        // Add collected orders count to title
        val title = requireActivity().toolbar.title
        val count = requireContext().getString(R.string.number_things, orderList.size.toString())
        if (!title.contains(count)) {
            requireActivity().toolbar.title = "$title   $count"
        }

        // Add collected orders to adapter
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

        // Set courier click listener
        adapter.setOnCourierClickListener { orderId ->
            val action = CollectedOrdersFragmentDirections
                .actionCollectedOrdersFragmentToCollectedTransferFragment(orderId = orderId)
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
        // Add collected orders count to title
        val title = requireActivity().toolbar.title
        val count = requireContext().getString(R.string.number_things, 0.toString())
        if (!title.contains(count)) {
            requireActivity().toolbar.title = "$title   $count"
        }

        // Add collected orders to adapter
        adapter.addOrders(orders = listOf())
    }


}