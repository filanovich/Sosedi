package by.imlab.sosedi.ui.scanprinter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.startRotateAnimation
import by.imlab.sosedi.ui.global.extentions.toast
import kotlinx.android.synthetic.main.search_printer_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class ScanPrinterFragment : Fragment(R.layout.scan_printer_fragment) {

    private val viewModel: ScanPrinterViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Listen printer connection state
        viewModel.printerScanState.observe(viewLifecycleOwner) { state -> render(state = state) }

        // Start rotation
        syncIcon.startRotateAnimation()

        // Listen barcode scan and validate
        viewModel.barcode.observe(viewLifecycleOwner) { barcode ->
            viewModel.validateAndConnect(barcode = barcode)
        }
    }

    private fun render(state: ScanPrinterState) {
        when (state) {
            is ScanPrinterState.Connecting -> renderConnecting()
            is ScanPrinterState.WrongBarcode -> renderWrongBarcode()
            is ScanPrinterState.PrinterConnect -> renderPrinterConnect()
            is ScanPrinterState.PrinterConnectError -> renderPrinterConnectError()
        }
    }

    private fun renderPrinterConnect() {
        toast(R.string.printer_connected)
        val action = ScanPrinterFragmentDirections
            .actionScanPrinterFragmentToOrdersListFragment()
        findNavController().navigate(action)
    }

    private fun renderPrinterConnectError() {
        toast(R.string.printer_is_not_responding)
    }

    private fun renderWrongBarcode() {
        toast(R.string.barcode_is_wrong)
    }

    private fun renderConnecting() {
        toast(R.string.connecting)
    }

    companion object {
        private const val SCANNER_RESULT = "nlscan.action.SCANNER_RESULT"
    }
}