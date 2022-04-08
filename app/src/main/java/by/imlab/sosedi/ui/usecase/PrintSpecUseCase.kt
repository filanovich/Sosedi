package by.imlab.sosedi.ui.usecase

import android.content.Context
import androidx.lifecycle.MutableLiveData
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.ui.global.helpers.PrintState
import by.imlab.sosedi.ui.global.helpers.PrinterHelper
import by.imlab.sosedi.ui.global.print.PrintBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PrintSpecUseCase(private val context: Context, private val printerHelper: PrinterHelper) {

    private val state = MutableLiveData<PrintState>().default(PrintState.Suspense)
    val printState get() = state

    private lateinit var order: OrderWithEntities

    fun print(orderWithEntities: OrderWithEntities) {
        order = orderWithEntities

        // Set state to printing
        state.set(PrintState.Printing)

        // Generate and print bitmap
        val bitmap = PrintBitmap.generateSpecificationLabel(context, orderWithEntities)
        CoroutineScope(Dispatchers.IO).launch {
            printerHelper.printBitmap(bitmap).collect {
                state.set(it)
            }
        }
    }

    fun printRetry() {
        print(orderWithEntities = order)
    }

}