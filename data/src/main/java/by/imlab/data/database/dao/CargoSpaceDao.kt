package by.imlab.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import by.imlab.data.database.entity.CargoSpaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CargoSpaceDao : BaseDao<CargoSpaceEntity> {

//    @Query("SELECT * FROM cargospaceentity WHERE orderId = :orderId")
//    suspend fun getSpacesByOrderId(orderId: Long): List<CargoSpaceEntity>

    @Query("SELECT * FROM cargospaceentity WHERE orderId = :orderId")
    fun getSpacesByOrderId(orderId: Long): List<CargoSpaceEntity>

    @Query("SELECT * FROM cargospaceentity WHERE orderId = :orderId")
    fun getSpacesFlowByOrderId(orderId: Long): Flow<List<CargoSpaceEntity>>

    @Query("DELETE FROM cargospaceentity WHERE orderId = :orderId")
    fun deleteAllByOrderId(orderId: Long)

}