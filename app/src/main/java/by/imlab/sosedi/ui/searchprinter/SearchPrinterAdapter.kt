package by.imlab.sosedi.ui.searchprinter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.imlab.data.model.Printer
import by.imlab.sosedi.databinding.PrinterItemBinding

class SearchPrinterAdapter(private val viewModel: SearchPrinterViewModel) :
    RecyclerView.Adapter<SearchPrinterAdapter.PrinterViewHolder>() {

    private val data: MutableList<Printer> = mutableListOf()

    fun addPrinter(printer: Printer) {
        if (!data.contains(printer)) {
            data.add(printer)
            notifyDataSetChanged()
        }
    }

    fun clearList() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PrinterViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrinterViewHolder {
        return PrinterViewHolder(
            PrinterItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) { mac -> viewModel.printerConnect(mac = mac) }
    }

    override fun getItemCount(): Int = data.size

    class PrinterViewHolder(
        private val binding: PrinterItemBinding,
        private val connect: (String) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Printer) {
            binding.apply {
                printer = item
                executePendingBindings()
            }

            binding.bluetoothConnect.setOnClickListener {
                connect(item.mac)
            }
        }
    }

}