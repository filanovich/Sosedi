package by.imlab.sosedi.ui.labelprint

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.setDivider
import by.imlab.sosedi.ui.global.extentions.toast
import kotlinx.android.synthetic.main.label_print_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LabelPrintFragment : BaseFragment(R.layout.label_print_fragment) {

    private val viewModel: LabelPrintViewModel by viewModel()
    private val args: LabelPrintFragmentArgs by navArgs()
    private val adapter by lazy { LabelPrintAdapter() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set divider for printed labels list
        printedLabels.setDivider(R.drawable.recycler_view_divider)

        // Setup LabelPrintAdapter
        printedLabels.adapter = adapter

        // Check printing state
        viewModel.printState.observe(viewLifecycleOwner) { state ->
            renderPrintState(state = state)
        }

        // Fetch current order
        val orderId = args.orderId
        viewModel.fetchOrderById(orderId = orderId)

        // Check fetching current order
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state = state) }
    }

    private fun render(state: LabelPrintState) {
        when (state) {
            is LabelPrintState.OrderSuccess -> renderSuccess(
                cargoSpaces = state.cargoSpaces,
                addSpacesAllow = state.addSpacesAllow
            )
            is LabelPrintState.OrderError -> renderError()
        }
    }

    private fun renderSuccess(
        cargoSpaces: List<CargoSpaceEntity>,
        addSpacesAllow: Boolean
    ) {
        totalCargoSpaces.text = cargoSpaces.sumBy { it.spaceIds.size }.toString()

        // Set data to adapter
        adapter.addCargoSpaces(cargoSpaces)
        adapter.setOnPrintLabelClickListener {
            viewModel.printCargoSpace(it)
        }

        // Hide when spaces is more than goods
        addOneMoreText.isVisible = addSpacesAllow
        openCargoPlaceButton.isVisible = addSpacesAllow

        // Open cargo place listener
        val orderId = args.orderId
        openCargoPlaceButton.setOnClickListener {
            val action = LabelPrintFragmentDirections
                .actionLabelPrintFragmentToOpenCargoNavigation(orderId = orderId)
            findNavController().navigate(action)
        }
    }

    private fun renderError() {
        toast(getString(R.string.there_is_no_label_to_print))
    }

    override fun onPrintSuccess() {

    }

    override fun onPrintRetry() {
        viewModel.printRetry()
    }
}