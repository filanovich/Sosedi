package by.imlab.sosedi.ui.global.adapters

import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.model.BarcodeType
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.ScannedBarcodeItemBinding

class ScannedBarcodeAdapter(
    private val barcodeList: MutableList<BarcodeEntity>,
    private var listener: ((BarcodeEntity) -> Unit)
) : RecyclerView.Adapter<ScannedBarcodeAdapter.BarcodeViewHolder>() {

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {
        holder.bind(barcodeList[position])
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BarcodeViewHolder {
        return BarcodeViewHolder(
            ScannedBarcodeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) { barcode ->
            barcodeList.remove(barcode)
            listener(barcode)
            notifyDataSetChanged()
        }
    }

    class BarcodeViewHolder(
        private val binding: ScannedBarcodeItemBinding,
        private val listener: ((BarcodeEntity) -> Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BarcodeEntity) {
            binding.apply {
                barcode = item
                executePendingBindings()
            }

            // Set number value
            binding.number.text = when (item.getType()) {
                BarcodeType.CustomWeight -> {
                    val formatValue = DecimalFormat("#0.000")
                        .format(item.getQuantity())
                    binding.number.context.getString(
                        R.string.number_kilograms,
                        formatValue
                    )
                }
                else -> {
                    binding.number.context.getString(
                        R.string.number_things,
                        item.getQuantity().toInt().toString()
                    )
                }
            }

            // Set drop button listener
            binding.dropButton.setOnClickListener {
                listener(item)
            }
        }
    }

    fun addBarcode(barcodeEntity: BarcodeEntity) {
        barcodeList.add(barcodeEntity)
        notifyDataSetChanged()
    }

    override fun getItemCount() = barcodeList.size
}