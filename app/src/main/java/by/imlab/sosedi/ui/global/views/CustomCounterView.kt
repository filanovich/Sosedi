package by.imlab.sosedi.ui.global.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import by.imlab.data.model.BarcodeType
import by.imlab.sosedi.R
import kotlinx.android.synthetic.main.custom_counter_view.view.*

class CustomCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var currentValue: String = "0"
    private var barcodeType: BarcodeType? = null

    init {
        View.inflate(context, R.layout.custom_counter_view, this)
    }

    fun setBarcodeType(barcodeType: BarcodeType) {
        this.barcodeType = barcodeType
        updateValue()
    }

    fun updateCounter(value: String) {
        currentValue = value
        updateValue()
    }

    private fun updateValue() {
        when (barcodeType) {
            BarcodeType.CustomGoods -> {
                number.text = context.getString(R.string.number_things, currentValue)
                addOneMoreText.text = context.getString(R.string.add_another_piece_item)
            }
            BarcodeType.CustomWeight -> {
                number.text = context.getString(R.string.number_kilograms, currentValue)
                addOneMoreText.text = context.getString(R.string.add_another_weight_item)
            }
        }
    }

}