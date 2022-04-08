package by.imlab.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.imlab.data.database.deserializer.DateDeserializer
import by.imlab.data.model.BarcodeType
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*

@Entity
data class BarcodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val skuId: Long = 0,
    val value: String,
    @JsonDeserialize(using = DateDeserializer::class)
    val scannedDate: Date = Date()
) {
    fun getType(): BarcodeType {
        return when {
            value.startsWith(CUSTOM_GOODS_PREFIX) && value.length >= 12 -> BarcodeType.CustomGoods
            value.startsWith(CUSTOM_WEIGHT_PREFIX) && value.length >= 12 -> BarcodeType.CustomWeight
            else -> BarcodeType.CommonGoods
        }
    }

    fun getCode(): String {
        val isWeightCustomProduct = value.startsWith(CUSTOM_WEIGHT_PREFIX) && value.length >= 12
        val isThingsCustomProduct = value.startsWith(CUSTOM_GOODS_PREFIX) && value.length >= 12
        return if (isWeightCustomProduct || isThingsCustomProduct) {
            value.substring(PRODUCT_CODE_RANGE)
        } else {
            value
        }
    }

    fun getQuantity(): Double {
        return when (getType()) {
            BarcodeType.CommonGoods -> 1.0 // For this type, the quantity is always equal to one
            BarcodeType.CustomGoods -> value.substring(WEIGHT_QUANTITY_RANGE).toDouble()
            BarcodeType.CustomWeight -> {
                val kilo = value.substring(WEIGHT_KILO_RANGE).toDouble()
                val gram = value.substring(WEIGHT_GRAM_RANGE).toDouble() / 1000
                return kilo + gram
            }
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        // Barcode of weighted products (custom barcode of the warehouse)
        const val CUSTOM_WEIGHT_PREFIX = "20"

        // Barcode of piece products marked through a scale (custom barcode of the warehouse)
        private const val CUSTOM_GOODS_PREFIX = "21"

        // Get weight in kilo from barcode "20AAAAABBСССX" (BB - is weight in kilo)
        private val WEIGHT_KILO_RANGE = IntRange(7, 8)

        // Get weight in gram from barcode "20AAAAABBСССX" (CCC - is weight in gram)
        private val WEIGHT_GRAM_RANGE = IntRange(9, 11)

        // Get quantity in things from barcode "21AAAAABBBBBX" (BBBBB - is quantity in things)
        private val WEIGHT_QUANTITY_RANGE = IntRange(7, 11)

        // Get product code from barcode "20AAAAABBСССX" or "21AAAAABBBBBX" (AAAAA - is product code)
        private val PRODUCT_CODE_RANGE = IntRange(2, 6)
    }
}