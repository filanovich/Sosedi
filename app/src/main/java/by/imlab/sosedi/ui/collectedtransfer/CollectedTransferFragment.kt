package by.imlab.sosedi.ui.collectedtransfer

import android.app.AlertDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.setDivider
import by.imlab.sosedi.ui.global.extentions.toast
import by.imlab.sosedi.ui.global.utils.FormatUtils
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.collected_transfer_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.dsl.koinApplication
import java.util.*

class CollectedTransferFragment : Fragment(R.layout.collected_transfer_fragment) {

    private val viewModel: CollectedTransferViewModel by viewModel()
    private val args: CollectedTransferFragmentArgs by navArgs()
    private val adapter by lazy { CollectedTransferAdapter() }

    private var spotsDialog: AlertDialog? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        spotsDialog = SpotsDialog.Builder()
            .setCancelable(false)
            .setContext(requireContext()).build()

        // Set divider for cargo spaces list
        cargoSpaces.setDivider(R.drawable.recycler_view_divider)

        // Setup SearchPrinterAdapter
        cargoSpaces.adapter = adapter

        // Fetch current order
        val orderId = args.orderId
        viewModel.fetchOrderById(orderId = orderId)

        // Listen scanned barcode changes
        viewModel.fetchScannedBarcode()

        // Check fetching current order
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state = state) }

        // Transfer button listener
        transferButton.setOnClickListener {
            transferButton.visibility = View.INVISIBLE
            viewModel.checkOrderDone()
        }
    }

    private fun render(state: CollectedTransferState) {
        when (state) {
            is CollectedTransferState.OrderSuccess -> renderSuccess(orderWithEntities = state.orderWithEntities)
            is CollectedTransferState.OrderError -> renderError()
            is CollectedTransferState.ScanSuccess -> renderScanSuccess(cargoSpaces = state.cargoSpaces)
            is CollectedTransferState.ScanError -> renderScanError()
            is CollectedTransferState.TransferSuccess -> renderTransferSuccess(
                order = state.order,
                transferredAt = state.transferredAt
            )
            is CollectedTransferState.TransferError -> renderTransferError(state.message)

            is CollectedTransferState.SpotsDialogOn -> spotsDialog?.show()
            is CollectedTransferState.SpotsDialogOff -> spotsDialog?.dismiss()
        }
    }

    private fun renderSuccess(orderWithEntities: OrderWithEntities) {
        val order = orderWithEntities.order

        // Set data to adapter
        adapter.addCargoSpaces(orderWithEntities.cargoSpaceList)

        // Main details
        orderNumber.text = order.number
        orderAddress.text = order.address

        // Scanned labels
        val scanned = orderWithEntities.cargoSpaceList.count { it.scanned }
        val total = orderWithEntities.cargoSpaceList.size
        scannedLabels.text =
            FormatUtils.formatFromToNumbers(requireContext(), R.string.from_is, scanned.toDouble(), total.toDouble())

        // Sku data
        val collectedSku = orderWithEntities.getCollectedSkuCount()
        val totalSku = orderWithEntities.skuList.size
        totalCollectedSkuCount.text = FormatUtils.formatFromToNumbers(
            requireContext(),
            R.string.from_things,
            collectedSku.toDouble(),
            totalSku.toDouble()
        )

        // Goods data
        val collectedGoods = orderWithEntities.getCollectedGoodsCount()
        val totalGoods = orderWithEntities.getTotalGoods()
        totalCollectedProductsCount.text =
            FormatUtils.formatFromToNumbers(
                requireContext(),
                R.string.from_things,
                collectedGoods,
                totalGoods
            )

        // Info button listener
        infoButton.setOnClickListener {
            val action = CollectedTransferFragmentDirections
                .actionCollectedTransferFragmentToCollectedSpecFragment(orderId = order.id)
            findNavController().navigate(action)
            viewModel.resetState()
        }
    }

    private fun renderError() {
        TODO("Not yet implemented")
    }

    private fun renderScanSuccess(cargoSpaces: List<CargoSpaceEntity>) {
        // Update scanned labels
        val scanned = cargoSpaces.count { it.scanned }
        val total = cargoSpaces.size
        scannedLabels.text =
            FormatUtils.formatFromToNumbers(requireContext(), R.string.from_is, scanned.toDouble(), total.toDouble())

        // Update cargo spaces list
        adapter.addCargoSpaces(cargoSpaces)
    }

    private fun renderScanError() {
        dialog(
            title = R.string.label_is_not_found_in_the_order,
            message = R.string.you_can_not_scan_the_label_from_another_order,
            positiveText = R.string.go_back,
            positive = {}
        )
    }

    private fun renderTransferSuccess(
        order: OrderEntity,
        transferredAt: Date
    ) {
        val formatDate = SimpleDateFormat("dd.MM.yy hh:mm:ss", Locale.ROOT).format(transferredAt)
        val title = getString(R.string.successfully_transferred_to_the_courier)
        val message = getString(
            R.string.order_number_and_transferred_time,
            order.number,
            formatDate
        )
        val positiveText = getString(R.string.ok)
        dialog(
            title = title,
            message = message,
            positiveText = positiveText,
            positive = {
                findNavController().navigateUp()
            }
        )
    }

    private fun renderTransferError(message: String?) {
        transferButton.visibility = View.VISIBLE

        if (message == null) {
            dialog(
                title = R.string.not_all_labels_scanned,
                message = R.string.you_must_scan_all_labels_to_transfer,
                positiveText = R.string.go_back,
                positive = {}
            )
        } else {
            toast(message)
        }
    }

}