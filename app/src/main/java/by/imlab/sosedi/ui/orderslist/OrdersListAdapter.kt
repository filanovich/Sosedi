package by.imlab.sosedi.ui.orderslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.OrderStatus
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.OrderItemBinding

class OrdersListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data: MutableList<OrderWithEntities> = mutableListOf()
    private var acceptOrderListener: ((order: OrderEntity) -> Unit)? = null
    private var hasUnderway = false

    fun setOnAcceptOrder(listener: (order: OrderEntity) -> Unit) {
        this.acceptOrderListener = listener
    }

    fun addOrders(orders: List<OrderWithEntities>, hasUnderway: Boolean) {
        data.clear()
        data.addAll(orders)
        this.hasUnderway = hasUnderway
        notifyDataSetChanged()
    }

    fun clearList() {
        hasUnderway = false;
        data.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position], hasUnderway)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            acceptOrderListener,
            OrderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val acceptOrderListener: ((order: OrderEntity) -> Unit)?,
        private val binding: OrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderWithEntities, hasUnderway: Boolean) {
            binding.apply {
                val order = item.order
                val skuListSize = item.skuList.count().toString()
                val context = binding.root.context

                orderButton.isVisible = !hasUnderway
                orderDate.text = order.getFormatDate()
                orderNumber.text = order.number
                orderCount.text = skuListSize
                assemblyTime.text = context.getString(R.string.assembly_time, skuListSize)
                orderStatus.text = context.getString(R.string.queue)

                orderButton.setOnClickListener {
                    acceptOrderListener?.invoke(item.order)
                    true
                }
            }
        }
    }

}