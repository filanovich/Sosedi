package by.imlab.sosedi.ui.collectedtransfer

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.model.SpaceType
import by.imlab.sosedi.R
import by.imlab.sosedi.databinding.CollectedTransferItemBinding

class CollectedTransferAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data: MutableList<CargoSpaceEntity> = mutableListOf()

    fun addCargoSpaces(orders: List<CargoSpaceEntity>) {
        data.clear()
        data.addAll(orders)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            CollectedTransferItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = data.size

    class OrderViewHolder(
        private val binding: CollectedTransferItemBinding
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

                if (item.scanned) {
                    cargoSpaceStatus.isVisible = false
                    statusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_check))
                }
            }
        }
    }

}