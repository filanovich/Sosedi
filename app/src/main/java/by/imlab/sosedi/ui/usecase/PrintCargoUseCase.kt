package by.imlab.sosedi.ui.usecase

import androidx.lifecycle.MutableLiveData
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.model.SpaceType
import by.imlab.data.repository.CargoSpaceRepository
import by.imlab.data.repository.OrderRepository
import by.imlab.sosedi.ui.global.helpers.PrintState
import by.imlab.sosedi.ui.global.helpers.PrinterHelper
import by.imlab.sosedi.ui.global.print.PrintBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PrintCargoUseCase(
    private val printerHelper: PrinterHelper,
    private val cargoSpaceRepository: CargoSpaceRepository,
    private val orderRepository: OrderRepository
) {

    private var printType = PrintType.SpaceLabel

    private var printing = false

    private val state = MutableLiveData<PrintState>().default(PrintState.Suspense)
    val printState get() = state

    private var numberSpaces: Int = 0
    private var spacesIds = listOf<Int>()
    private lateinit var spaceType: SpaceType
    private lateinit var cargoSpace: CargoSpaceEntity

    // Order details
    private var orderId: Long = 0
    private lateinit var orderNumber: String

    fun print(type: SpaceType, orderId: Long, orderNumber: String, number: Int = 1) {
        spaceType = type
        this.orderId = orderId
        this.orderNumber = orderNumber
        numberSpaces = number

        // Set state to printing
        state.set(PrintState.Printing)

        // Fetch available spaces
        if (!printing) fetchAvailableSpaces()

        // Printing type set
        printType = PrintType.SpaceLabel
    }

    fun print(type: SpaceType, order: OrderEntity, number: Int = 1) {
        spaceType = type
        orderId = order.id
        orderNumber = order.number
        numberSpaces = number

        // Set state to printing
        state.set(PrintState.Printing)

        // Fetch available spaces
        if (!printing) fetchAvailableSpaces()

        // Printing type set
        printType = PrintType.SpaceLabelReprint
    }

    fun print(orderNumber: String, cargo: CargoSpaceEntity) {
        this.orderNumber = orderNumber
        spacesIds = cargo.spaceIds

        // Set state to printing
        state.set(PrintState.Printing)

        val order: OrderEntity = orderRepository.fetchOrderByNumber(orderNumber)

        // Generate bitmap
        val bitmap = PrintBitmap.generateCargoSpaceLabel(
            order = order,
            spacesIds = cargo.spaceIds
        )
        CoroutineScope(Dispatchers.IO).launch {
            printerHelper.printBitmap(bitmap).collect {
                state.set(it)
            }
        }

        // Printing type set
        printType = PrintType.SpaceCargo
    }

    fun printRetry() {
        // Set state to printing
        state.set(PrintState.Printing)

        when (printType) {
            PrintType.SpaceCargo -> {
                // Generate bitmap

                val order: OrderEntity = orderRepository.fetchOrderByNumber(orderNumber)

                val bitmap = PrintBitmap.generateCargoSpaceLabel(
                    order = order,
                    spacesIds = spacesIds
                )
                CoroutineScope(Dispatchers.IO).launch {
                    printerHelper.printBitmap(bitmap).collect {
                        state.set(it)
                    }
                }
            }
            else -> {
                // Fetch available spaces
                if (!printing) fetchAvailableSpaces()
            }
        }
    }

    private fun fetchAvailableSpaces() {
        printing = true
        CoroutineScope(Dispatchers.IO).launch {
            val spaces = cargoSpaceRepository.getSpacesByOrderId(orderId = orderId)
            generate(spaces)
        }
    }

    private fun generate(cargoSpaces: List<CargoSpaceEntity>) {
        val lastCargoId =
            if (cargoSpaces.isNotEmpty() && cargoSpaces.last().spaceIds.isNotEmpty()) {
                cargoSpaces.last().spaceIds.last()
            } else {
                0
            }

        val firstId = lastCargoId.plus(1) // Cargo place id begins from 1, not from 0 :(
        val lastId = lastCargoId.plus(numberSpaces)
        val spacesIds =
            if (numberSpaces > 1) IntRange(firstId, lastId).toList() else listOf(firstId)

        // Generate cargo space
        val noSeparatedIds = spacesIds.joinToString("")

        var barcode = orderNumber.filter { it.isLetterOrDigit() }

        cargoSpace = CargoSpaceEntity(
            orderId = orderId,
            spaceIds = spacesIds,
            barcode = "$barcode$noSeparatedIds", //"$orderNumber ($noSeparatedIds)",
            type = spaceType
        )
        printLabel(spacesIds = spacesIds)
    }

    private fun printLabel(spacesIds: List<Int>) {

        val order: OrderEntity = orderRepository.fetchOrderByNumber(orderNumber)

        val bitmap = PrintBitmap.generateCargoSpaceLabel(
            order = order,
            spacesIds = spacesIds
        )
        CoroutineScope(Dispatchers.IO).launch {
            printerHelper.printBitmap(bitmap).collect {
                when (it) {
                    PrintState.Success -> {
                        if (printing) {
                            cargoSpaceRepository.createNewSpace(cargoSpace = cargoSpace)
                            state.set(PrintState.Success)
                        }
                    }
                    else -> state.set(it)
                }
                printing = false
            }
        }
    }

    enum class PrintType {
        SpaceLabel,
        SpaceLabelReprint,
        SpaceCargo
    }

}