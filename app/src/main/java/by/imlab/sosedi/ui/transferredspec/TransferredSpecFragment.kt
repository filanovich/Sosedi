package by.imlab.sosedi.ui.transferredspec

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.dialogWithSkuInfo
import by.imlab.sosedi.ui.global.extentions.setDivider
import by.imlab.sosedi.ui.global.utils.FormatUtils
import kotlinx.android.synthetic.main.transferred_spec_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferredSpecFragment : Fragment(R.layout.transferred_spec_fragment) {

    private val viewModel: TransferredSpecViewModel by viewModel()
    private val args: TransferredSpecFragmentArgs by navArgs()
    private val adapter: TransferredSpecAdapter by lazy { TransferredSpecAdapter() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set divider for printers list
        skuList.setDivider(R.drawable.recycler_view_divider)

        // Setup SearchPrinterAdapter
        skuList.adapter = adapter

        // Fetch order by id
        viewModel.fetchOrderById(id = args.orderId)

        // Check transfer spec state
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state = state)
        }
    }

    private fun render(state: TransferredSpecState) {
        when (state) {
            is TransferredSpecState.OrderSuccess -> renderOrderSuccess(orderWithEntities = state.orderWithEntities)
            is TransferredSpecState.OrderError -> renderOrderError(throwable = state.throwable)
        }
    }

    private fun renderOrderSuccess(orderWithEntities: OrderWithEntities) {
        val order = orderWithEntities.order

        // Main details
        orderNumber.text = order.number
        orderAddress.text = order.address

        // Scanned labels
        val cargoSpacesCount = orderWithEntities.cargoSpaceList.sumBy { it.spaceIds.count() }
        numberCargoSpaces.text = cargoSpacesCount.toString()

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
        totalCollectedProductsCount.text = FormatUtils.formatFromToNumbers(
            requireContext(),
            R.string.from_things,
            collectedGoods,
            totalGoods
        )

        // Set adapter list and PopupMenu listener
        adapter.addSkuList(skuList = orderWithEntities.skuList)
        adapter.setOnPopupMenuClickListener { menuId, skuId ->
            when (menuId) {
                R.id.details -> showSkuInfoDialog(orderWithEntities, skuId)
            }
        }
    }

    // TODO: Change error text
    private fun renderOrderError(throwable: Throwable) {
        dialog(
            title = R.string.no_underway_orders,
            message = R.string.take_another_order_to_underway,
            positiveText = R.string.go_back
        ) { requireActivity().onBackPressed() }
    }

    private fun showSkuInfoDialog(orderWithEntities: OrderWithEntities, skuId: Long) {
        val skuWithEntities = orderWithEntities
            .skuList.find { it.sku.id == skuId } ?: return
        dialogWithSkuInfo(
            skuWithEntities = skuWithEntities,
            positiveText = R.string.go_back,
            positive = {}
        )
    }
}