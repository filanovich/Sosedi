package by.imlab.sosedi.ui.global.views

import android.content.Context
import android.icu.text.DecimalFormat
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.model.BarcodeType
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.adapters.ScannedBarcodeAdapter
import by.imlab.sosedi.ui.global.extentions.dialogWithBarcodeList
import by.imlab.sosedi.ui.global.extentions.dialogWithInputWeight
import kotlinx.android.synthetic.main.custom_counter_view.*
import kotlinx.android.synthetic.main.scanned_barcode_layout.view.*

class ScannedBarcodeView @JvmOverloads constructor(
    fragment: Fragment,
    sku: SkuEntity,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    barcodeList: MutableList<BarcodeEntity> = arrayListOf(),
    onDropListener: ((BarcodeEntity) -> Unit)? = null,
    onNewListener: ((BarcodeEntity) -> Unit)? = null
) : FrameLayout(fragment.requireContext(), attrs, defStyleAttr) {

    private var barcodeType: BarcodeType? = null
    private var localBarcodeList: MutableList<BarcodeEntity> = arrayListOf()
    private var scannedBarcodeAdapter: ScannedBarcodeAdapter? = null

    init {
        inflate(fragment.requireContext(), R.layout.scanned_barcode_layout, this)

        // Setup local list and type
        barcodeType = BarcodeType.CustomWeight
        localBarcodeList = barcodeList

        // Setup initial data
        updateData()

        // Set list view content
        scannedBarcodeAdapter = ScannedBarcodeAdapter(barcodeList = localBarcodeList) { barcode ->
            // Update info after setting new list values
            updateData()
            // Set drop barcode upper
            onDropListener?.invoke(barcode)
        }

        scannedList.adapter = scannedBarcodeAdapter

        // Listen add button
        addButton.setOnClickListener {
            dialogWithInputWeight(
                fragment,
                sku,
                positiveText = R.string.ok
            ) { weight ->
                //var id = 0L
                //if (localBarcodeList.size > 0)
                //    id = localBarcodeList.maxOf { it.id }

                val weightStr = (weight * 1000).toInt().toString()
                val value = BarcodeEntity.CUSTOM_WEIGHT_PREFIX +
                        sku.barcodeId.padStart(5, '0') +
                        weightStr.padStart(5, '0')

                val barcodeEntity = BarcodeEntity(skuId = sku.id, value = value)

                scannedBarcodeAdapter?.addBarcode(barcodeEntity)

                updateData()

                onNewListener?.invoke(barcodeEntity)

                //viewModel.updateScannedList(it)
            }
        }

    }

    private fun updateData() {

        if (!localBarcodeList.isEmpty())
            barcodeType = localBarcodeList.first().getType()

        val totalQuantity = localBarcodeList.map { it.getQuantity() }.sum()

        // Set number value
        when (barcodeType) {
            BarcodeType.CustomGoods -> {
                number.text =
                    context.getString(R.string.number_things, totalQuantity.toInt().toString())
                indicateQuantityText.text =
                    context.getString(R.string.quantity_of_marked_products)
            }
            BarcodeType.CustomWeight -> {
                val formatValue = DecimalFormat("#0.000").format(totalQuantity)
                number.text = context.getString(R.string.number_kilograms, formatValue)
                indicateQuantityText.text =
                    context.getString(R.string.weight_scanned_products)
            }
        }
    }

}