package by.imlab.data.database.model

import androidx.room.Embedded
import androidx.room.Relation
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.entity.SkuEntity


data class OrderWithEntities(
    @Embedded
    val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId", entity = SkuEntity::class)
    val skuList: List<SkuWithEntities>,
    @Relation(parentColumn = "id", entityColumn = "orderId", entity = CargoSpaceEntity::class)
    val cargoSpaceList: List<CargoSpaceEntity>
) {
    fun isCollected(): Boolean {
        return getCollectedSkuCount() == skuList.count()
    }

    fun getCollectedSkuCount(): Int {
        return skuList.count { it.isCollected() }
    }

    fun getCollectedGoodsCount(): Double {
        return skuList.sumOf { it.getCollected() }
    }

    fun getTotalGoods(): Double {
        return skuList.sumOf { it.getTotal() }
    }

    fun getCurrentSku(): SkuWithEntities? {
        return getSortedSkuList().find { !it.isCollected() }
    }

    fun getNextSku(): SkuWithEntities? {
        return getSortedSkuList().find { !it.isCollected() && it.sku.id != getCurrentSku()?.sku?.id }
    }

    fun checkBarcodeInOrder(barcodeEntity: BarcodeEntity): Boolean {
        return skuList.any {
            val c = barcodeEntity.getCode()
            val t = barcodeEntity.getType()
            it.sku.barcodeId == barcodeEntity.getCode() && it.sku.barcodeType == barcodeEntity.getType()
        }
    }

    fun checkBarcodeIsCurrent(barcodeEntity: BarcodeEntity): Boolean {
        val currentSku = getCurrentSku()?.sku
        return currentSku?.barcodeId == barcodeEntity.getCode() && currentSku.barcodeType == barcodeEntity.getType()
    }

    private fun getSortedSkuList(): List<SkuWithEntities> {
        return skuList.sortedBy { it.getCollected() }
    }

    fun getAllowedSpacesNumber(): Int {
        val numberSpaces = cargoSpaceList.sumOf { it.spaceIds.size }
        val allowedSpaces = getTotalGoods() - numberSpaces
        return if (allowedSpaces > 99) 99 else allowedSpaces.toInt()
    }

    fun isAddSpacesAllow(): Boolean {
        return getAllowedSpacesNumber() > 0
    }
}