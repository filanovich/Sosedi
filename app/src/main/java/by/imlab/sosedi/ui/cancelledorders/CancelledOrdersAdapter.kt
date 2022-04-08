package by.imlab.sosedi.ui.cancelledorders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.OrderEntity
import by.imlab.sosedi.databinding.CancelledOrdersItemBinding

class CancelledOrdersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data: MutableList<OrderEntity> = mutableListOf()

    fun addOrders(orders: List<OrderEntity>) {
        data.clear()
        data.addAll(orders)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            CancelledOrdersItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val binding: CancelledOrdersItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderEntity) {
            binding.apply {
                order = item
                executePendingBindings()
            }
        }
    }

}