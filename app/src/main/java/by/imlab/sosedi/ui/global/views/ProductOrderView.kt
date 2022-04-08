package by.imlab.sosedi.ui.global.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.model.BarcodeType
import by.imlab.sosedi.R
import kotlinx.android.synthetic.main.product_order_view.view.*

class ProductOrderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        inflate(context, R.layout.product_order_view, this)
    }

    fun setupView(currentSku: SkuEntity) {
        // Setup view depends on the barcode type
        name.text = currentSku.name
        barcode.text = currentSku.barcodeId.toString()
        quantity.text = formatQuantity(currentSku = currentSku)
    }

    private fun formatQuantity(currentSku: SkuEntity): String {
        return when (currentSku.barcodeType) {
            BarcodeType.CustomWeight -> context.getString(
                R.string.number_kilograms,
                currentSku.quantity.toString()
            )
            BarcodeType.CustomGoods -> context.getString(
                R.string.number_things,
                currentSku.quantity.toInt().toString()
            )
            BarcodeType.CommonGoods -> context.getString(
                R.string.number_things,
                currentSku.quantity.toInt().toString()
            )
        }
    }
}