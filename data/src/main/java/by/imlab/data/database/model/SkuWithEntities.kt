package by.imlab.data.database.model

import androidx.room.Embedded
import androidx.room.Relation
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.model.BarcodeType
import kotlin.math.abs

data class SkuWithEntities(
    @Embedded val sku: SkuEntity,
    @Relation (parentColumn = "id", entityColumn = "skuId")
    val scannedCodes: MutableList<BarcodeEntity> = mutableListOf()
) {
    fun isCollected(): Boolean {
        if (scannedCodes.isEmpty()) return false
        return when (sku.barcodeType) {
            BarcodeType.CommonGoods -> sku.quantity.toInt() == scannedCodes.size
            else -> {
                true
/*                val requiredQuality = sku.quantity
                val scannedQuality = scannedCodes.map { it.getQuantity() }.sum()

                // Permissible error in weight not more than 10% in both directions
                abs(100 * (requiredQuality - scannedQuality) / scannedQuality) < 10
 */
            }
        }
    }

    fun getCollected(): Double {
        return when (sku.barcodeType) {
            BarcodeType.CommonGoods -> scannedCodes.size.toDouble()
            else -> scannedCodes.map { it.getQuantity() }.sum()
            //BarcodeType.CustomGoods -> scannedCodes.map { it.getQuantity() }.sum().toDouble()
            //BarcodeType.CustomWeight -> if (isCollected()) 1 else 0
        }
    }

    fun getTotal(): Double {
        return when (sku.barcodeType) {
            BarcodeType.CustomWeight -> {
                return sku.quantity;
                //1
                //val kilo = sku.quantity value.substring(BarcodeEntity.WEIGHT_KILO_RANGE).toFloat()
                 //   val gram = value.substring(BarcodeEntity.WEIGHT_GRAM_RANGE).toFloat() / 1000
                 //   return kilo + gram

            }
            else -> sku.quantity
        }
    }
}
