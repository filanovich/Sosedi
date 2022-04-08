package by.imlab.sosedi.ui.specification

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.BaseFragment
import by.imlab.sosedi.ui.global.extentions.*
import kotlinx.android.synthetic.main.specification_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SpecificationFragment : BaseFragment(R.layout.specification_fragment, true) {

    private val viewModel: SpecificationViewModel by viewModel()
    private val args: SpecificationFragmentArgs by navArgs()
    private val adapter: SpecificationAdapter by lazy { SpecificationAdapter() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set divider for printers list
        skuList.setDivider(R.drawable.recycler_view_divider)

        // Setup SearchPrinterAdapter
        skuList.adapter = adapter

        // Fetch collection order
        viewModel.fetchOrderById(id = args.orderId)

        // Fetch scanned barcode
        viewModel.fetchScannedBarcode()

        // Check order package state
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state = state)
        }

        // Check printing state
        viewModel.printState.observe(viewLifecycleOwner) { state ->
            renderPrintState(state = state)
        }
    }

    private fun render(state: SpecificationState) {
        when (state) {
            is SpecificationState.OrderSuccess -> renderOrderSuccess(orderWithEntities = state.orderWithEntities)
            is SpecificationState.OrderError -> renderOrderError(throwable = state.throwable)
            is SpecificationState.ScanSuccess -> renderScanSuccess(
                skuId = state.skuId,
                orderNumber = state.orderNumber,
                barcode = state.barcode,
                allowedSpaces = state.allowedSpaces
            )
            is SpecificationState.ScanError -> renderScanError()
            is SpecificationState.NotEnoughError -> renderNotEnoughError()
            is SpecificationState.CollectionSuccess -> renderCollectionSuccess(order = state.order)
            is SpecificationState.CollectionError -> renderCollectionError(message = state.message)
            is SpecificationState.StockBalanceSuccess -> renderStockBalanceSuccess(message = state.value)
            is SpecificationState.StockBalanceError -> renderStockBalanceError(message = state.message)
            is SpecificationState.ImageSuccess -> renderImageSuccess(image = state.image)
            is SpecificationState.ImageError -> renderImageError(message = state.message)
        }
    }

    private fun renderOrderSuccess(orderWithEntities: OrderWithEntities) {
        val order = orderWithEntities.order
        val cargoSpaces = orderWithEntities.cargoSpaceList
        val cargoSpacesCount = cargoSpaces.sumOf { it.spaceIds.count() }

        // Set specification details
        orderNumber.text = order.number
        cargoSpaceNumber.text = cargoSpacesCount.toString()

        // Set adapter list and PopupMenu listener
        adapter.setCanEdit(canEdit = args.canEdit)
        adapter.addSkuList(skuList = orderWithEntities.skuList)

        adapter.setOnMiniatureClickListener { sku ->
            viewModel.getImage(sku)
        }

        adapter.setOnPopupMenuClickListener { menuId, sku ->
            when (menuId) {
                R.id.details -> showSkuInfoDialog(sku.id)
                R.id.changeNumber -> renderScanSuccess(
                    skuId = sku.id,
                    orderNumber = order.number,
                    barcode = "",
                    allowedSpaces = orderWithEntities.getAllowedSpacesNumber()
                )
                R.id.enterCodeManually -> showManuallyEnterDialog()
                R.id.productNoFound -> viewModel.resetScannedCodes(skuId = sku.id)
                R.id.requestStockBalance -> viewModel.getStockBalance(sku)
            }
        }

        // Order info button listener
        infoButton.setOnClickListener {
            showOrderInfoDialog(orderWithEntities)
        }

        // Complete order collection listener
        collectButton.setOnClickListener {
            viewModel.validateOrderCollection(skipValidation = false)
        }

        editButton.setOnClickListener {
            val action = SpecificationFragmentDirections
                .actionSpecificationFragmentToLabelPrintFragment(orderId = order.id)
            findNavController().navigate(action)
        }
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
        barcode: String,
        allowedSpaces: Int
    ) {
        val action = SpecificationFragmentDirections
            .actionSpecificationFragmentToSkuDetailsFragment(
                skuId = skuId,
                orderNumber = orderNumber,
                barcode = barcode,
                allowedSpaces = allowedSpaces
            )
        findNavController().navigate(action)
        viewModel.resetState()
    }

    private fun renderScanError() {

    }

    private fun renderNotEnoughError() {
        dialog(
            title = R.string.not_all_products_are_collected_in_the_order,
            message = R.string.anyway_move_to_the_collected,
            positiveText = R.string.complete,
            positive = { viewModel.validateOrderCollection(true) },
            negativeText = R.string.cancel,
            negative = {})
    }

    private fun renderCollectionSuccess(order: OrderEntity) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null)
            viewModel.printSpecificationLabel()
        else
            onPrintSuccess()
    }

    private fun renderCollectionError(message: String?) {
        if (message != null) {
            dialog(
                message = message,
                positiveText = getString(R.string.ok)
            ) {}
        }
    }

    /*
        val title =
            if (requireContext().checkNetworkState()) getString(R.string.no_connection_to_server)
            else getString(R.string.no_internet_connection)
        val message = getString(R.string.Unable_to_retrieve_stock_balances)
        val positiveText = getString(R.string.go_back)
        dialog(
            title = title,
            message = message,
            positiveText = positiveText,
            positive = { }
        )
*/

    private fun renderStockBalanceSuccess(message: String?) {
        if (message != null) {
            dialog(
                message = message,
                positiveText = getString(R.string.ok)
            ) {}
        }
    }

    private fun renderStockBalanceError(message: String?) {
        if (message != null) {
            dialog(
                message = message,
                positiveText = getString(R.string.ok)
            ) {}
        }
    }

    private fun renderImageSuccess(image: String?) {
        if (image != null) {
            dialogWithImage(
                imageBase64 = image,
                positiveText = R.string.go_back,
                positive = {})
        }
    }

    private fun renderImageError(message: String?) {
        if (message != null) {
            dialog(
                message = message,
                positiveText = getString(R.string.ok)
            ) {}
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
                val action = SpecificationFragmentDirections
                    .actionSpecificationFragmentToOrdersListFragment()
                findNavController().navigate(action)
            }
        )
    }

    override fun onPrintRetry() {
        viewModel.printRetry()
    }

    private fun showSkuInfoDialog(skuId: Long) {
        val skuWithEntities = viewModel.orderWithEntities
            .skuList.find { it.sku.id == skuId } ?: return

        dialogWithSkuInfo(
            skuWithEntities = skuWithEntities,
            positiveText = R.string.go_back,
            positive = {})
    }

    private fun showOrderInfoDialog(orderWithEntities: OrderWithEntities) {
        dialogWithOrderInfo(
            orderWithEntities = orderWithEntities,
            positiveText = R.string.go_back,
            positive = {})
    }

    private fun showManuallyEnterDialog() {
        dialogWithInputBarcode(
            positive = { viewModel.validateBarcode(barcode = it) },
            negative = {}
        )
    }
}