package by.imlab.sosedi.ui.collectedspec

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.model.SkuWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.CollectedSpecItemBinding

class CollectedSpecAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data: MutableList<SkuWithEntities> = mutableListOf()


    fun addSkuList(skuList: List<SkuWithEntities>) {
        data.clear()
        data.addAll(skuList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            CollectedSpecItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val binding: CollectedSpecItemBinding
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
            }
        }
    }

}