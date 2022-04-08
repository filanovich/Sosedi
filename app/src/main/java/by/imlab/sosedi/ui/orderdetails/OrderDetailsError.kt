package by.imlab.sosedi.ui.orderdetails

sealed class OrderDetailsError {
    object NotInOrder : OrderDetailsError()
    object OutOfTurn : OrderDetailsError()
}