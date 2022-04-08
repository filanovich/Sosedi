package by.imlab.sosedi.ui.specification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.database.model.SkuWithEntities
import by.imlab.data.model.BarcodeType
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.SpecificationItemBinding
import by.imlab.sosedi.ui.cargospase.CargoSpaceState
import by.imlab.sosedi.ui.global.extentions.formatTrim

class SpecificationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var canEdit: Boolean = true
    private var listener: ((menuId: Int, sku: SkuEntity) -> Unit)? = null
    private var miniatureListener: ((sku: SkuEntity) -> Unit)? = null
    private val data: MutableList<SkuWithEntities> = mutableListOf()

    fun setCanEdit(canEdit: Boolean) {
        this.canEdit = canEdit
    }

    fun addSkuList(skuList: List<SkuWithEntities>) {
        data.clear()
        val list = skuList.sortedBy { it.sku.category }
        data.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnMiniatureClickListener(listener: (sku: SkuEntity) -> Unit) {
        this.miniatureListener = listener
    }

    fun setOnPopupMenuClickListener(listener: (menuId: Int, sku: SkuEntity) -> Unit) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            canEdit,
            listener,
            miniatureListener,
            SpecificationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val canEdit: Boolean,
        private val listener: ((menuId: Int, sku: SkuEntity) -> Unit)?,
        private val miniatureListener: ((sku: SkuEntity) -> Unit)?,
        private val binding: SpecificationItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SkuWithEntities) {
            binding.apply {
                val currentSku = item.sku
                val context = binding.root.context

                // Set sku details

                if (currentSku.image.isNullOrEmpty())
                    miniature.visibility = View.GONE
                else {
                    miniature.visibility = View.VISIBLE
                    miniature.setImageBitmap(currentSku.convertToImage());
                }

                name.text = currentSku.name
                category.text = currentSku.category
                barcode.text = currentSku.barcodeId
                val resId = if (item.sku.barcodeType == BarcodeType.CustomWeight)
                    R.string.from_things_kg else R.string.from_things_pcs

                quantity.text =
                    context.getString(
                        resId,
                        item.getCollected().formatTrim(),
                        item.getTotal().formatTrim()
                    )

                if (!canEdit) dotsMenu.isVisible = false
                // Init product dots menu
                val popupMenu = PopupMenu(context, dotsMenu)
                popupMenu.menuInflater.inflate(R.menu.specification_item_menu, popupMenu.menu)

                // Dots menu click listener
                dotsMenu.setOnClickListener { popupMenu.show() }

                // Dots menu item click listener
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    listener?.invoke(menuItem.itemId, item.sku)
                    true
                }

                // Dots menu item click listener
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    listener?.invoke(menuItem.itemId, item.sku)
                    true
                }

                // Miniature click listener
                miniature.setOnClickListener {
                    miniatureListener?.invoke(item.sku)
                    true
                }
            }
        }
    }

}