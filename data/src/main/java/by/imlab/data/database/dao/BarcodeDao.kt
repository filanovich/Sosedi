package by.imlab.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import by.imlab.data.database.entity.BarcodeEntity


@Dao
interface BarcodeDao : BaseDao<BarcodeEntity> {

    @Query("DELETE FROM barcodeentity WHERE skuId = :skuId")
    fun deleteAllBySkuId(skuId: Long)

    @Transaction
    fun insertAndCleanOld(skuId: Long, newCodes: List<BarcodeEntity>) {
        deleteAllBySkuId(skuId = skuId)
        insert(list = newCodes)
    }

}