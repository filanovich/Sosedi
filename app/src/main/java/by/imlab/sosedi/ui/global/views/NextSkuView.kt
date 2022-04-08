package by.imlab.sosedi.ui.global.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import by.imlab.data.database.entity.SkuEntity
import by.imlab.sosedi.R
import kotlinx.android.synthetic.main.next_sku_view.view.*

class NextSkuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var sku: SkuEntity? = null
        set(value) {
            field = value
            updateValues()
        }

    init {
        inflate(context, R.layout.next_sku_view, this)

        // Init default values
        updateValues()
    }

    private fun updateValues() {
        category.text = sku?.category ?: "-"
        name.text = sku?.name ?: "-"
    }

}