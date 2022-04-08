package by.imlab.data.model

enum class OrderStatus(val value: Int) {
    QUEUE(0),
    UNDERWAY(1),
    COLLECTION(2),
    TRANSFER(3),
    CANCEL(4)
}