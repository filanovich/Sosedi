package by.imlab.sosedi.ui.collectedorders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.OrderEntity
import by.imlab.sosedi.databinding.CollectedOrdersItemBinding

class CollectedOrdersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: ((orderId: Long) -> Unit)? = null
    private val data: MutableList<OrderEntity> = mutableListOf()

    fun addOrders(orders: List<OrderEntity>) {
        data.clear()
        data.addAll(orders)
        notifyDataSetChanged()
    }

    fun setOnCourierClickListener(listener: (orderId: Long) -> Unit) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            listener,
            CollectedOrdersItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val listener: ((orderId: Long) -> Unit)?,
        private val binding: CollectedOrdersItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderEntity) {
            binding.apply {
                order = item
                executePendingBindings()

                sendCourier.setOnClickListener {
                    listener?.invoke(item.id)
                }
            }
        }
    }

}