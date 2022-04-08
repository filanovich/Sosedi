package by.imlab.data.model

enum class BarcodeType(val value: Int) {
    CommonGoods(0),
    CustomGoods(1),
    CustomWeight(2);

    companion object {
        fun valueOf(value: Int): BarcodeType? = BarcodeType.values().find { it.value == value }
    }
}