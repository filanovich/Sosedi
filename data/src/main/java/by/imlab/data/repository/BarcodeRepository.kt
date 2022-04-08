package by.imlab.data.repository

import by.imlab.data.database.dao.BarcodeDao
import by.imlab.data.database.entity.BarcodeEntity

class BarcodeRepository(private val barcodeDao: BarcodeDao) {

    fun updateScannedCodes(skuId: Long, scannedCodes: MutableList<BarcodeEntity>) =
        barcodeDao.insertAndCleanOld(skuId = skuId, newCodes = scannedCodes)

    fun resetScannedCodes(skuId: Long) =
        barcodeDao.insertAndCleanOld(skuId = skuId, newCodes = mutableListOf())

}