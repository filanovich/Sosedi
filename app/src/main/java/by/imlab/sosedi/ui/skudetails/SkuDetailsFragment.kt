package by.imlab.sosedi.ui.skudetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.imlab.data.database.model.SkuWithEntities
import by.imlab.data.model.BarcodeType
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.OrdersListFragmentBinding
import by.imlab.sosedi.databinding.SkuDetailsFragmentBinding
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.*
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.views.CommonCounterView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_counter_view.*
import kotlinx.android.synthetic.main.sku_details_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalStdlibApi
class SkuDetailsFragment : BaseFragment(R.layout.sku_details_fragment) {

    private val viewModel: SkuDetailsViewModel by viewModel()
    private val args: SkuDetailsFragmentArgs by navArgs()
    private var scanningDialog: AlertDialog? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set appbar title
        val orderNumber = args.orderNumber
        requireActivity().toolbar.title = getString(R.string.order_number_is, orderNumber)

        // Fetch scanned barcode
        viewModel.fetchScannedBarcode()

        // Fetch collection order
        val skuId = args.skuId
        viewModel.fetchSkuById(id = skuId, orderNumber = orderNumber)

        // Check sku details state
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state = state)
        }

        // Check printing state
        viewModel.printState.observe(viewLifecycleOwner) { state ->
            renderPrintState(state = state)
        }

        // Listen edit counter button
        editButton.setOnClickListener {
            dialogWithBarcodeList(
                viewModel.currentSku,
                barcodeList = viewModel.scannedCodes,
                positiveText = R.string.go_back
            ) {
                viewModel.updateScannedList(it)
            }
        }

        // Hide barrier when check checkbox
        countWithoutPackage.setOnCheckedChangeListener { _, isChecked ->
            viewModel.useCargoSpaces(isChecked)
            barrier.isVisible = !isChecked
        }

        // Disable adding spaces when it is more than goods
        openSpacesBlock.isVisible = args.allowedSpaces > 0

        collectButton.setOnClickListener {
            viewModel.validateSkuCollection(skipValidation = false)
        }
    }

    private fun render(state: SkuDetailsState) {
        when (state) {
            is SkuDetailsState.SkuSuccess -> renderSkuSuccess(skuWithEntities = state.skuWithEntities)
            is SkuDetailsState.SkuError -> renderOrderError()
            is SkuDetailsState.ScanCommonSuccess -> renderScanCommonSuccess(currentValue = state.currentValue)
            is SkuDetailsState.ScanCustomSuccess -> renderScanCustomSuccess(currentValue = state.currentValue)
            is SkuDetailsState.ScanError -> renderScanError()
            is SkuDetailsState.ExcessError -> renderExcessError(barcodeType = state.barcodeType)
            is SkuDetailsState.CollectionSuccess -> renderCollectionSuccess()
            is SkuDetailsState.NotEnoughError -> renderNotEnoughError(barcodeType = state.barcodeType)
        }
    }

    private fun renderSkuSuccess(skuWithEntities: SkuWithEntities) {
        val currentSku = skuWithEntities.sku

        // Set product order details
        productOrderDetails.setupView(currentSku = currentSku)

        // Show counter for different types and setup scanned value
        when (val type = currentSku.barcodeType) {
            BarcodeType.CommonGoods -> {
                commonTypeCounter.isVisible = true
            }
            else -> {
                customTypeCounter.setBarcodeType(type)
                customTypeCounter.isVisible = true
            }
        }

        // Scanned barcode on the last screen
        val barcode = args.barcode
        if (barcode.isNotEmpty()) {
            viewModel.validateBarcode(PrinterBarcode(barcode))
        }

        // Listen add barcode button
        addMoreButton.setOnClickListener {
            scanningDialog?.show()
        }

        if (currentSku.barcodeType == BarcodeType.CommonGoods) {
            commonTypeCounter.updateMaxValue(maxValue = skuWithEntities.getTotal().toInt())
            if (skuWithEntities.isCollected()) {
                commonTypeCounter.updateCurrentValue(value = skuWithEntities.getCollected().toInt())
            }
        }

        // Common counter listeners
        commonTypeCounter.setOnChangeValueListener { action ->
            when (action) {
                CommonCounterView.Action.Plus -> viewModel.addCommonScannedBarcode()
                CommonCounterView.Action.Minus -> viewModel.removeCommonScannedBarcode()
            }
        }

        // Number spaces listener
        numberPlacesCounter.setOnChangeValueListener { number ->
            viewModel.updateSpacesNumber(number)
        }

        // Set max and initial value for number places counter and add one bag
        numberPlacesCounter.updateMaxValue(maxValue = args.allowedSpaces)

        // Create dialog
        scanningDialog = createDialogWithScanning(
            barcodeType = currentSku.barcodeType,
            positiveText = R.string.enter_manually,
            positive = {
                dialogWithInputBarcode(
                    positive = { barcode -> viewModel.validateBarcode(barcode) },
                    negative = {}
                )
            },
            negativeText = R.string.cancel,
            negative = {}
        )
    }

    override fun onPrintSuccess() {
        viewModel.updateSkuAndContinue()
    }

    override fun onPrintRetry() {
        viewModel.printRetry()
    }

    private fun renderOrderError() {

    }

    private fun renderScanCommonSuccess(currentValue: Int) {
        scanningDialog?.hide()
        commonTypeCounter.updateCurrentValue(value = currentValue)
    }

    private fun renderScanCustomSuccess(currentValue: String) {
        scanningDialog?.hide()
        customTypeCounter.updateCounter(value = currentValue)
    }

    private fun renderScanError() {
        toast(R.string.barcode_is_wrong)
    }

    private fun renderExcessError(barcodeType: BarcodeType) {
        scanningDialog?.hide()
        when (barcodeType) {
            BarcodeType.CustomWeight -> dialog(
                title = R.string.weight_product_error,
                message = R.string.amount_is_more_than_ten_percent,
                positiveText = R.string.go_back
            ) {}
            else -> dialog(
                title = R.string.things_product_error,
                message = R.string.amount_is_more_than_ten_percent,
                positiveText = R.string.go_back
            ) {}
        }
    }

    private fun renderCollectionSuccess() {
        findNavController().navigateUp()
        viewModel.resetState()
    }

    private fun renderNotEnoughError(barcodeType: BarcodeType) {
        val title = when (barcodeType) {
            BarcodeType.CustomWeight -> R.string.not_enough_weight
            BarcodeType.CommonGoods -> R.string.not_enough_common_things
            BarcodeType.CustomGoods -> R.string.not_enough_things
        }
        val message = R.string.are_you_sure_you_want_to_collect_not_in_full
        dialog(
            title = title,
            message = message,
            positiveText = R.string.to_collect,
            positive = { viewModel.validateSkuCollection(true) },
            negativeText = R.string.cancel,
            negative = {})
    }

    private fun createDialogWithScanning(
        barcodeType: BarcodeType,
        @StringRes positiveText: Int,
        positive: () -> Unit,
        @StringRes negativeText: Int,
        negative: () -> Unit
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
        builder.setCancelable(false)

        // Add input field to dialog
        val viewInflater = LayoutInflater.from(this.context)
            .inflate(R.layout.dialog_scan_layout, this.requireView() as ViewGroup, false)

        // Set scan wait info text
        val scanWaitText = viewInflater.findViewById<AppCompatTextView>(R.id.scanWaitText)
        scanWaitText.text = when (barcodeType) {
            BarcodeType.CustomGoods -> requireContext().getString(R.string.scan_things_product_to_add)
            else -> requireContext().getString(R.string.scan_weight_product_to_add)
        }

        // Start icon rotation
        viewInflater.findViewById<ImageView>(R.id.syncIcon).startRotateAnimation()

        // Setup view
        builder.setView(viewInflater)

        // Buttons listeners
        builder.setPositiveButton(getString(positiveText).toUpperCase()) { _, _ -> positive() }
        builder.setNegativeButton(getString(negativeText).toUpperCase()) { _, _ -> negative() }

        // Create and show dialog
        return builder.create()
    }

}