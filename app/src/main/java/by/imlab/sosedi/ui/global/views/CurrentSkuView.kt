package by.imlab.sosedi.ui.global.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import by.imlab.data.database.model.SkuWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.startRotateAnimation
import kotlinx.android.synthetic.main.current_sku_view.view.*


class CurrentSkuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var skuWithEntities: SkuWithEntities? = null
        set(value) {
            field = value
            updateValues()
        }

    init {
        inflate(context, R.layout.current_sku_view, this)

        // Init default values
        updateValues()
    }

    private fun updateValues() {
        val sku = skuWithEntities?.sku
        val barcodeId = if (sku?.barcodeId != null) sku.barcodeId.toString() else "-"

        category.text = sku?.category ?: "-"
        name.text = sku?.name ?: "-"
        barcode.text = barcodeId

        skuWithEntities?.getTotal()?.let {
            quantity.text = context.getString(R.string.number_things, it.toString())
        } ?: run {
            quantity.text = "-"
        }

        // Hide elements when no product available
        dotsMenu.isVisible = skuWithEntities != null
        syncIcon.isVisible = skuWithEntities != null
        scanWaitText.isVisible = skuWithEntities != null

        // Start sync icon animation
        syncIcon.startRotateAnimation()
    }

}