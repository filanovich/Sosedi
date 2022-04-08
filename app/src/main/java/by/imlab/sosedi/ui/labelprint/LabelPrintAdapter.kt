package by.imlab.sosedi.ui.labelprint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.model.SpaceType
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.LabelPrintItemBinding

class LabelPrintAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: ((CargoSpaceEntity) -> Unit)? = null
    private val data: MutableList<CargoSpaceEntity> = mutableListOf()

    fun addCargoSpaces(orders: List<CargoSpaceEntity>) {
        data.clear()
        data.addAll(orders)
        notifyDataSetChanged()
    }

    fun setOnPrintLabelClickListener(listener: (CargoSpaceEntity) -> Unit) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            listener,
            LabelPrintItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val listener: ((CargoSpaceEntity) -> Unit)?,
        private val binding: LabelPrintItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CargoSpaceEntity) {
            binding.apply {
                val context = binding.root.context

                val commaSeparatedIds = item.spaceIds.joinToString(",")
                cargoSpaceNumber.text = if (item.type == SpaceType.POCKET) {
                    context.getString(R.string.bag_number_is, commaSeparatedIds)
                } else {
                    context.getString(R.string.cargo_space_number_is, commaSeparatedIds)
                }

                printButton.setOnClickListener {
                    listener?.invoke(item)
                }
            }
        }
    }

}