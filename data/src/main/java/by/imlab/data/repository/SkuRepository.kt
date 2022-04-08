package by.imlab.data.repository

import by.imlab.data.database.dao.SkuDao
import by.imlab.data.database.entity.SkuEntity

class SkuRepository(
    private val skuDao: SkuDao
) {

    fun deleteAll() = skuDao.deleteAll()

    fun fetchSkuWithEntitiesById(id: Long) = skuDao.getSkuWithEntitiesById(id)

    fun moveSkuToLastPosition(sku: SkuEntity) = skuDao.moveToLastPosition(sku = sku)
}