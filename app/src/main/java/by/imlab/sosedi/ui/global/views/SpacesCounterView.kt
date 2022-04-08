package by.imlab.sosedi.ui.global.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import by.imlab.sosedi.R
import kotlinx.android.synthetic.main.common_counter_view.view.*

class SpacesCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var currentValue: Int = 1
    private val minValue: Int = 1
    private var maxValue: Int = 0
    private var title: String = ""
    private var listener: ((Int) -> Unit)? = null

    init {
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.CommonCounterView, 0, 0
        )
        title = attributes.getString(R.styleable.CommonCounterView_title) ?: "-"
        attributes.recycle()

        inflate(context, R.layout.common_counter_view, this)

        indicateQuantityText.text = title

        // Calculate buttons listeners
        minus.setOnClickListener {
            updateValues(action = Action.Minus)
            listener?.invoke(currentValue)
        }

        plus.setOnClickListener {
            updateValues(action = Action.Plus)
            listener?.invoke(currentValue)
        }

        // Set default value to $minValue and hide minus button
        number.text = minValue.toString()
        minus.isVisible = false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // Calculate buttons listeners
        minus.setOnClickListener {
            updateValues(action = Action.Minus)
            listener?.invoke(currentValue)
        }
    }

    fun setOnChangeValueListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    fun updateValues(action: Action) {
        when (action) {
            Action.Plus -> {
                val newValue = currentValue.plus(1)
                if (newValue < maxValue) {
                    currentValue = currentValue.plus(1)
                } else if (newValue == maxValue) {
                    currentValue = currentValue.plus(1)
                    plus.isVisible = false
                }
                minus.isVisible = true
            }
            Action.Minus -> {
                val newValue = currentValue.minus(1)
                if (newValue > minValue) {
                    currentValue = currentValue.minus(1)
                } else if (newValue == minValue) {
                    currentValue = currentValue.minus(1)
                    minus.isVisible = false
                }
                plus.isVisible = true
            }
        }

        number.text = currentValue.toString()
    }

    fun updateMaxValue(maxValue: Int) {
        this.maxValue = maxValue
        if (maxValue == currentValue) {
            minus.isVisible = false
            plus.isVisible = false
        }
    }

    fun getCurrentValue(): Int {
        return currentValue
    }

    sealed class Action {
        object Plus : Action()
        object Minus : Action()
    }

}