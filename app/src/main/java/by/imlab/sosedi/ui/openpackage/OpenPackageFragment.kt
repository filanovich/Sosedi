package by.imlab.sosedi.ui.openpackage

import android.os.Bundle
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import by.imlab.core.extensions.set
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.SpaceType
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.dialogWithCounter
import by.imlab.sosedi.ui.global.extentions.startRotateAnimation
import by.imlab.sosedi.ui.specification.SpecificationState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.open_package_fragment.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OpenPackageFragment : BaseFragment(R.layout.open_package_fragment) {

    private val viewModel: OpenPackageViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //openSpaceButton.isEnabled = false

        // Fetch underway order
        viewModel.fetchUnderwayOrder()

        // Check order package state
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state = state)
        }

        // Check printing state
        viewModel.printState.observe(viewLifecycleOwner) { state ->
            renderPrintState(state = state)
        }

        // Fetch scanned barcode
        viewModel.fetchScannedBarcode()

        // Start rotate animation
        syncIcon.startRotateAnimation()
    }

    private fun render(state: OpenPackageState) {
        when (state) {
            is OpenPackageState.OrderSuccess -> renderOrderSuccess(order = state.order)
            is OpenPackageState.OrderError -> renderOrderError(throwable = state.throwable)
            is OpenPackageState.WrongCodeError -> renderWrongCodeError()
        }
    }

    private fun renderOrderSuccess(order: OrderWithEntities) {
        requireActivity().toolbar.title = getString(R.string.order_number_is, order.order.number)

        //viewModel.printCargoSpace(SpaceType.CARGO, 1)

        // Open cargo space button listener
        openSpaceButton.setOnClickListener {
            dialogWithCounter(
                title = R.string.specify_the_number_of_packages,
                positiveText = R.string.save,
                positive = {
                    viewModel.printCargoSpace(SpaceType.CARGO, it)
                },
                negativeText = R.string.cancel,
                negative = {},
                allowedSpaces = order.getAllowedSpacesNumber()
            )
        }
    }

    private fun renderOrderError(throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onPrintSuccess() {
        //val action = OpenPackageFragmentDirections.actionOpenPackageFragmentToOrderDetailsFragment()
        val order = viewModel.order
        val action = OpenPackageFragmentDirections
            .actionOpenPackageFragmentToSpecificationFragment(
                orderId = order.id,
                canEdit = true
            )
        findNavController().navigate(action)
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