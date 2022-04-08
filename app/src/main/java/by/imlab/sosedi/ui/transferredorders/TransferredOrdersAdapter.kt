package by.imlab.sosedi.ui.transferredorders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.OrderEntity
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.TransferredOrdersItemBinding

class TransferredOrdersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: ((orderId: Long) -> Unit)? = null
    private val data: MutableList<OrderEntity> = mutableListOf()

    fun addOrders(orders: List<OrderEntity>) {
        data.clear()
        data.addAll(orders)
        notifyDataSetChanged()
    }

    fun setOnPopupMenuClickListener(listener: (orderId: Long) -> Unit) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            listener,
            TransferredOrdersItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val listener: ((orderId: Long) -> Unit)?,
        private val binding: TransferredOrdersItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderEntity) {
            binding.apply {
                order = item
                executePendingBindings()

                val context = root.context

                // Init product dots menu
                val popupMenu = PopupMenu(context, dotsMenu)
                popupMenu.menuInflater.inflate(R.menu.transferred_orders_item_menu, popupMenu.menu)

                // Dots menu click listener
                dotsMenu.setOnClickListener { popupMenu.show() }

                // Dots menu item click listener
                popupMenu.setOnMenuItemClickListener {
                    listener?.invoke(item.id)
                    true
                }
            }
        }
    }

}