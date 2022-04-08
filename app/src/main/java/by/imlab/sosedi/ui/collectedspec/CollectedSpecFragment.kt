package by.imlab.sosedi.ui.collectedspec

import android.os.Bundle
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.dialogWithOrderInfo
import by.imlab.sosedi.ui.global.extentions.setDivider
import kotlinx.android.synthetic.main.collected_spec_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollectedSpecFragment : BaseFragment(R.layout.collected_spec_fragment, true) {

    private val viewModel: CollectedSpecViewModel by viewModel()
    private val args: CollectedSpecFragmentArgs by navArgs()
    private val adapter: CollectedSpecAdapter by lazy { CollectedSpecAdapter() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set divider for printers list
        skuList.setDivider(R.drawable.recycler_view_divider)

        // Setup CollectedSpecAdapter
        skuList.adapter = adapter

        // Check printing state
        viewModel.printState.observe(viewLifecycleOwner) { state ->
            renderPrintState(state = state)
        }

        // Fetch order by id
        viewModel.fetchOrderById(id = args.orderId)

        // Check collected spec state
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state = state)
        }
    }

    private fun render(state: CollectedSpecState) {
        when (state) {
            is CollectedSpecState.OrderSuccess -> renderOrderSuccess(orderWithEntities = state.orderWithEntities)
            is CollectedSpecState.OrderError -> renderOrderError(throwable = state.throwable)
        }
    }

    private fun renderOrderSuccess(orderWithEntities: OrderWithEntities) {
        val order = orderWithEntities.order
        val cargoSpaces = orderWithEntities.cargoSpaceList
        val cargoSpacesCount = cargoSpaces.sumBy { it.spaceIds.count() }

        // Set specification details
        orderNumber.text = order.number
        cargoSpaceNumber.text = cargoSpacesCount.toString()

        // Set adapter list
        adapter.addSkuList(skuList = orderWithEntities.skuList)

        // Order info button listener
        infoButton.setOnClickListener {
            showOrderInfoDialog(orderWithEntities)
        }

        // Print spec button listener
        printButton.setOnClickListener {
            viewModel.printSpecificationLabel()
        }

        // Edit button listener
        editButton.setOnClickListener {
            val action = CollectedSpecFragmentDirections
                .actionCollectedSpecFragmentToLabelPrintFragment(orderId = order.id)
            findNavController().navigate(action)
        }
    }

    // TODO: Change error text
    private fun renderOrderError(throwable: Throwable) {
        dialog(
            title = R.string.no_underway_orders,
            message = R.string.take_another_order_to_underway,
            positiveText = R.string.go_back
        ) { requireActivity().onBackPressed() }
    }

    override fun onPrintSuccess() {

    }

    override fun onPrintRetry() {
        viewModel.printSpecificationLabel()
    }

    private fun showOrderInfoDialog(orderWithEntities: OrderWithEntities) {
        dialogWithOrderInfo(
            orderWithEntities = orderWithEntities,
            positiveText = R.string.go_back,
            positive = {})
    }
}