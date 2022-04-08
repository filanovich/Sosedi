package by.imlab.sosedi.ui.openpocket

import android.os.Bundle
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.startRotateAnimation
import kotlinx.android.synthetic.main.open_pocket_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class OpenPocketFragment : BaseFragment(R.layout.open_pocket_fragment) {

    private val viewModel: OpenPocketViewModel by viewModel()
    private val args: OpenPocketFragmentArgs by navArgs()

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

        // Fetch scanned barcode
        viewModel.fetchScannedBarcode()

        // Start icon rotate
        syncIcon.startRotateAnimation()
    }

    private fun render(state: OpenPocketState) {
        when (state) {
            is OpenPocketState.OrderError -> renderError()
            is OpenPocketState.WrongCodeError -> renderWrongCodeError()
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