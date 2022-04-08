package by.imlab.sosedi.ui.orderslist

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import kotlinx.android.synthetic.main.orders_list_main_item.view.*

class OrdersListMainItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var listner: (() -> Unit?)? = null

    init {
        View.inflate(context, R.layout.orders_list_main_item, this)

        orderButton.setOnClickListener {
            listner?.invoke()
        }
    }

    fun hideOrderButton() {
        orderButton.isVisible = false
        haveAnOrderError.isVisible = true
    }

    fun hideAllButtons() {
        orderButton.isVisible = false
        haveAnOrderError.isVisible = false
    }

    fun setOnAcceptListener(listener: () -> Unit) {
        this.listner = listener
    }

    fun setOrder(orderWithEntities: OrderWithEntities?) {
        val order = orderWithEntities?.order
        val skuListSize = orderWithEntities?.skuList?.count()

        orderButton.isVisible = order != null

        orderDate.text = order?.getFormatDate() ?: "-"
        orderNumber.text = order?.number ?: "-"
        orderCount.text = (skuListSize ?: "-").toString()
        assemblyTime.text =
            context.getString(R.string.assembly_time, (skuListSize ?: "-").toString())
    }

}