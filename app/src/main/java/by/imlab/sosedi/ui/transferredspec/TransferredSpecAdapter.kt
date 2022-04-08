package by.imlab.sosedi.ui.transferredspec

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.model.SkuWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.TransferredSpecItemBinding

class TransferredSpecAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: ((menuId: Int, skuId: Long) -> Unit)? = null
    private val data: MutableList<SkuWithEntities> = mutableListOf()


    fun addSkuList(skuList: List<SkuWithEntities>) {
        data.clear()
        data.addAll(skuList)
        notifyDataSetChanged()
    }

    fun setOnPopupMenuClickListener(listener: (menuId: Int, skuId: Long) -> Unit) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            listener,
            TransferredSpecItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val listener: ((menuId: Int, skuId: Long) -> Unit)?,
        private val binding: TransferredSpecItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SkuWithEntities) {
            binding.apply {
                val currentSku = item.sku
                val context = binding.root.context

                // Set sku details
                name.text = currentSku.name
                barcode.text = currentSku.barcodeId.toString()
                quantity.text =
                    context.getString(
                        R.string.from_things,
                        item.getCollected().toString(),
                        item.getTotal().toString()
                    )

                // Init product dots menu
                val popupMenu = PopupMenu(context, dotsMenu)
                popupMenu.menuInflater.inflate(R.menu.transfer_spec_menu, popupMenu.menu)

                // Dots menu click listener
                dotsMenu.setOnClickListener { popupMenu.show() }

                // Dots menu item click listener
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    listener?.invoke(menuItem.itemId, item.sku.id)
                    true
                }
            }
        }
    }

}