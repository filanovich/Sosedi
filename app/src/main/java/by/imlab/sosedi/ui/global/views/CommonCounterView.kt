package by.imlab.sosedi.ui.global.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import by.imlab.sosedi.R
import kotlinx.android.synthetic.main.common_counter_view.view.*

class CommonCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var currentValue: Int = 0
    private val minValue: Int = 0
    private var maxValue: Int = 0
    private var listener: ((Action) -> Unit)? = null

    init {
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.CommonCounterView, 0, 0
        )
        val title = attributes.getString(R.styleable.CommonCounterView_title) ?: "-"
        attributes.recycle()

        inflate(context, R.layout.common_counter_view, this)
        indicateQuantityText.text = title

        // Calculate buttons listeners
        minus.setOnClickListener {
            listener?.invoke(Action.Minus)
            updateValues(action = Action.Minus)
        }

        plus.setOnClickListener {
            listener?.invoke(Action.Plus)
            updateValues(action = Action.Plus)
        }

        // Set default value to $minValue and hide minus button
        number.text = minValue.toString()
        minus.isVisible = false
    }

    fun setOnChangeValueListener(listener: (Action) -> Unit) {
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
    }

    fun updateCurrentValue(value: Int) {
        this.currentValue = value
        number.text = value.toString()
        if (value > minValue) {
            minus.isVisible = true
        }
        if (value == maxValue) {
            plus.isVisible = false
        }
    }

    sealed class Action {
        object Plus : Action()
        object Minus : Action()
    }

}