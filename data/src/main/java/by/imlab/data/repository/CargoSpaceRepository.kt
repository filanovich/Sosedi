package by.imlab.data.repository

import by.imlab.data.database.dao.CargoSpaceDao
import by.imlab.data.database.entity.CargoSpaceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CargoSpaceRepository(private val cargoSpaceDao: CargoSpaceDao) {

    fun getSpacesByOrderId(orderId: Long) = cargoSpaceDao.getSpacesByOrderId(orderId)

    fun getSpacesFlowByOrderId(orderId: Long) = cargoSpaceDao.getSpacesFlowByOrderId(orderId)

    fun createNewSpace(cargoSpace: CargoSpaceEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            cargoSpaceDao.getSpacesFlowByOrderId(orderId = cargoSpace.orderId).collect { spaces ->
                if (!spaces.isNullOrEmpty()) {
                    val alreadyHave =
                        spaces.any { it.spaceIds.first() == cargoSpace.spaceIds.first() }
                    if (!alreadyHave) {
                        cargoSpaceDao.insert(cargoSpace)
                    }
                } else {
                    cargoSpaceDao.insert(cargoSpace)
                }
            }
        }
    }

    fun resetSpacesByOrderId(orderId: Long) = cargoSpaceDao.deleteAllByOrderId(orderId)
}