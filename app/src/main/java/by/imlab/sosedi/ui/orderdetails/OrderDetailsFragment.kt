package by.imlab.sosedi.ui.orderdetails

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.dialogWithInputBarcode
import by.imlab.sosedi.ui.global.extentions.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.collected_orders_item.*
import kotlinx.android.synthetic.main.current_sku_view.*
import kotlinx.android.synthetic.main.order_details_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrderDetailsFragment : BaseFragment(R.layout.order_details_fragment, true) {

    private val viewModel: OrderDetailsViewModel by viewModel()
    private var printed = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Fetch collection order
        viewModel.fetchCollectionOrder()

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

        // Init product dots menu
        val popupMenu = PopupMenu(requireContext(), dotsMenu)
        popupMenu.menuInflater.inflate(R.menu.current_sku_menu, popupMenu.menu)

        // Dots menu click listener
        dotsMenu.setOnClickListener { popupMenu.show() }

        // Dots menu item click listener
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.skipTemporarily -> viewModel.currentSkuToLastPosition()
                R.id.enterCodeManually -> {
                    dialogWithInputBarcode(
                        positive = { barcode -> viewModel.validateBarcode(barcode) },
                        negative = {}
                    )
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun render(state: OrderDetailsState) {
        when (state) {
            is OrderDetailsState.OrderSuccess -> renderOrderSuccess(orderWithEntities = state.orderWithEntities)
            is OrderDetailsState.OrderError -> renderOrderError(throwable = state.throwable)
            is OrderDetailsState.ScanSuccess -> renderScanSuccess(
                skuId = state.skuId,
                orderNumber = state.orderNumber,
                orderAddress = state.orderNumber,
                barcode = state.barcode,
                allowedSpaces = state.allowedSpaces
            )
            is OrderDetailsState.ScanError -> renderScanError(error = state.error)
            is OrderDetailsState.CollectionSuccess -> renderCollectionSuccess(order = state.order)
            is OrderDetailsState.CollectionError -> renderCollectionError(message = state.message)

        }
    }

    override fun onPrintSuccess() {
        val title = getString(R.string.order_collection_is_completed)
        val message = getString(
            R.string.order_number_and_completed_time,
            viewModel.order.number,
            viewModel.order.getFormatDate()
        )
        val positiveText = getString(R.string.to_new_orders)
        dialog(
            title = title,
            message = message,
            positiveText = positiveText,
            positive = {
                val action = OrderDetailsFragmentDirections
                    .actionOrderDetailsFragmentToOrdersListFragment()
                findNavController().navigate(action)
            }
        )
    }

    override fun onPrintRetry() {
        viewModel.printRetry()
    }

    private fun renderOrderSuccess(orderWithEntities: OrderWithEntities) {
        val order = orderWithEntities.order

        // Set appbar title
        requireActivity().toolbar.title = getString(R.string.order_number_is, order.number)

        // Cargo spaces number
        val number = orderWithEntities.cargoSpaceList.sumBy { it.spaceIds.size }
        cargoSpaceNumber.text = number.toString()

        // Hide add button when spaces more than products
        openCargoPlaceButton.isVisible = orderWithEntities.isAddSpacesAllow()

        // Set scanned sku
        val collectedSku = orderWithEntities.getCollectedSkuCount()
        val totalSku = orderWithEntities.skuList.size
        totalCollectedSkuCount.text = formatNumber(
            R.string.from_things, collectedSku.toDouble(), totalSku.toDouble()
        )

        // Set collected goods
        val collectedProducts = orderWithEntities.getCollectedGoodsCount()
        val totalProducts = orderWithEntities.getTotalGoods()
        totalCollectedProductsCount.text = formatNumber(
            R.string.from_things, collectedProducts, totalProducts
        )

        // Init current sku
        currentSkuDetails.skuWithEntities = orderWithEntities.getCurrentSku()

        // Set next sku
        nextSkuDetails.sku = orderWithEntities.getNextSku()?.sku

        // Open cargo place listener
        openCargoPlaceButton.setOnClickListener {
            val action = OrderDetailsFragmentDirections
                .actionOrderDetailsFragmentToOpenCargoNavigation(orderId = order.id)
            findNavController().navigate(action)
        }
    }

    private fun renderCollectionSuccess(order: OrderEntity) {
        if (printed) return
        printed = true
        viewModel.printSpecificationLabel()
    }

    private fun renderCollectionError(message: String?) {
        if (message != null) {
            toast(message)
        }
    }

    private fun formatNumber(textRes: Int, collected: Double, total: Double): Spanned {
        val strCollected = collected.toString().trimEnd('0', '.');
        val strTotal = total.toString().trimEnd('0', '.');
        val collectedFormatText =
            if (collected < total) "<b><font color='#000000'>$strCollected</font></b>"
            else "<font color='#00D358'>$strCollected</font>"

        val fromThings = getString(
            textRes,
            "",
            strTotal
        )

        return Html.fromHtml(collectedFormatText + fromThings, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun renderOrderError(throwable: Throwable) {
        dialog(
            title = R.string.no_underway_orders,
            message = R.string.take_another_order_to_underway,
            positiveText = R.string.go_back
        ) { requireActivity().onBackPressed() }
    }

    private fun renderScanSuccess(
        skuId: Long,
        orderNumber: String,
        orderAddress: String,
        barcode: String,
        allowedSpaces: Int
    ) {
        val action = OrderDetailsFragmentDirections.actionOrderDetailsFragmentToSkuDetailsFragment(
            skuId = skuId,
            orderNumber = orderNumber,
            barcode = barcode,
            allowedSpaces = allowedSpaces
        )
        findNavController().navigate(action)

        // Reset state to avoid opening sku details infinitely
        viewModel.resetState()
    }

    private fun renderScanError(error: OrderDetailsError) {
        when (error) {
            OrderDetailsError.NotInOrder -> {
                dialog(
                    R.string.this_product_is_not_in_order,
                    R.string.verify_the_scanned_barcode_is_correct,
                    R.string.go_back
                ) {}
            }
            OrderDetailsError.OutOfTurn -> {
                dialog(
                    R.string.product_scanned_out_of_turn,
                    R.string.follow_the_recommended_ordering_sequence,
                    R.string.go_back
                ) {}
            }
        }

        // Reset state to avoid opening sku details infinitely
        viewModel.makeOrdering()
    }

}