package by.imlab.sosedi.ui.cargospase

import android.os.Bundle
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.OpenCargoNavigationArgs
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.dialogWithCounter
import kotlinx.android.synthetic.main.cargo_space_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CargoSpaceFragment : BaseFragment(R.layout.cargo_space_fragment) {

    private val viewModel: CargoSpaceViewModel by viewModel()
    private val args: OpenCargoNavigationArgs by navArgs()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Fetch underway order
        val orderId = args.orderId
        viewModel.fetchOrderById(orderId = orderId)

        // Check fetch underway state
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state = state) }

        // Check printing state
        viewModel.printState.observe(viewLifecycleOwner) { state ->
            renderPrintState(state = state)
        }

        // Collection button click listener
        openBagButton.setOnClickListener {
            val action = CargoSpaceFragmentDirections
                .actionCargoSpaceFragmentToOpenPocketFragment(orderId = orderId)
            findNavController().navigate(action)
        }
    }

    private fun render(state: CargoSpaceState) {
        when (state) {
            is CargoSpaceState.OrderSuccess -> renderSuccess(orderWithEntities = state.orderWithEntities)
            is CargoSpaceState.OrderError -> renderError()
            is CargoSpaceState.WrongCodeError -> renderWrongCodeError()
        }
    }

    private fun renderSuccess(orderWithEntities: OrderWithEntities) {
        // Specification button click listener
        openCargoPlaceButton.setOnClickListener {
            dialogWithCounter(
                title = R.string.specify_the_number_of_packages,
                positiveText = R.string.save,
                positive = {
                    viewModel.printCargoSpace(it)
                },
                negativeText = R.string.cancel,
                negative = {},
                allowedSpaces = orderWithEntities.getAllowedSpacesNumber()
            )
        }
    }

    private fun renderError() {
        dialog(
            title = R.string.no_underway_orders,
            message = R.string.take_another_order_to_underway,
            positiveText = R.string.go_back
        ) { requireActivity().onBackPressed() }
    }

    override fun onPrintSuccess() {
        findNavController().navigateUp()
        viewModel.resetState()
    }

    override fun onPrintRetry() {
        viewModel.printRetry()
    }

    private fun renderWrongCodeError() {
        dialog(
            title = R.string.wrong_package_code,
            message = R.string.try_to_update_code_list_or_scan_another_one,
            positiveText = R.string.update,
            positive = {},
            negativeText = R.string.cancel,
            negative = {}
        )
    }
}