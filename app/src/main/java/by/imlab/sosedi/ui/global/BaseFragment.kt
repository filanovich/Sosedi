package by.imlab.sosedi.ui.global

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.startRotateAnimation
import by.imlab.sosedi.ui.global.helpers.PrintState
import com.google.android.material.dialog.MaterialAlertDialogBuilder


abstract class BaseFragment(
    @LayoutRes layoutId: Int,
    private val specification: Boolean = false
) : Fragment(layoutId) {

    private var lastState: PrintState? = null
    private val printingDialog: AlertDialog by lazy { createPrintingDialog() }

    internal fun renderPrintState(state: PrintState) {
        if (lastState == state) return
        when (state) {
            PrintState.Printing -> if (!printingDialog.isShowing) printingDialog.show()
            PrintState.Success -> onPrintSuccess()
            PrintState.PaperOutError -> renderPaperOutError()
            PrintState.HeadOpenError -> renderHeadOpenError()
            PrintState.ConnectionError -> renderConnectionError()
            PrintState.NotAvailableError -> renderNotAvailableError()
            PrintState.InternalError -> renderInternalError()
            PrintState.BluetoothError -> renderConnectionError() // TODO: Handle this case
        }
        if (state != PrintState.Printing) printingDialog.hide()
        lastState = state
    }

    abstract fun onPrintSuccess()

    abstract fun onPrintRetry()

    private fun renderPaperOutError() {
        dialog(
            title = R.string.no_paper,
            message = R.string.insert_the_label_and_ty_again,
            positiveText = R.string.retry,
            positive = { onPrintRetry() },
            negativeText = R.string.cancel,
            negative = {}
        )
    }

    private fun renderHeadOpenError() {
        dialog(
            title = R.string.head_not_closed,
            message = R.string.close_the_head_and_try_again,
            positiveText = R.string.retry,
            positive = { onPrintRetry() },
            negativeText = R.string.cancel,
            negative = {}
        )
    }

    private fun renderConnectionError() {
        dialog(
            title = R.string.printer_not_available,
            message = R.string.connect_the_printer_and_try_again,
            positiveText = R.string.connect,
            positive = { findNavController().navigate(R.id.searchPrinterFragment) },
            negativeText = R.string.cancel,
            negative = {}
        )
    }

    private fun renderNotAvailableError() {
        dialog(
            title = R.string.printer_not_available,
            message = R.string.connect_the_printer_and_try_again,
            positiveText = R.string.connect,
            positive = { findNavController().navigate(R.id.searchPrinterFragment) },
            negativeText = R.string.cancel,
            negative = {}
        )
    }

    private fun renderInternalError() {
        dialog(
            title = R.string.printer_error,
            message = R.string.check_your_printer_state,
            positiveText = R.string.retry,
            positive = { onPrintRetry() },
            negativeText = R.string.cancel,
            negative = {}
        )
    }

    private fun createPrintingDialog(): AlertDialog {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
        builder.setCancelable(false)

        // Add printing layout to dialog
        val viewInflater = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_print_layout, this.requireView() as ViewGroup, false)

        // Set spec waiting text
        if (specification) {
            viewInflater.findViewById<TextView>(R.id.scanWaitText).text =
                getString(R.string.spec_printing)
        }

        // Start icon rotation
        viewInflater.findViewById<ImageView>(R.id.syncIcon).startRotateAnimation()

        // Setup view
        builder.setView(viewInflater)

        // Create and show dialog
        return builder.create()
    }

}