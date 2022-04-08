package by.imlab.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao : BaseDao<OrderEntity> {

    @Query("DELETE FROM orderentity")
    fun deleteAll()

    @Query("SELECT * FROM orderentity")
    fun getOrdersWithEntities(): Flow<List<OrderWithEntities>>

    @Query("SELECT * FROM orderentity")
    fun getOrders(): List<OrderEntity>

    @Query("SELECT * FROM orderentity WHERE status = :status")
    fun getOrderByStatus(status: OrderStatus): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orderentity WHERE status = :status")
    fun getOrderWithEntitiesByStatus(status: OrderStatus): Flow<List<OrderWithEntities>>

    @Query("SELECT * FROM orderentity WHERE id = :id")
    fun getOrderById(id: Long): Flow<OrderEntity>

    @Query("SELECT * FROM orderentity WHERE number = :number")
    fun getOrderByNumber(number: String): OrderEntity

    @Query("SELECT * FROM orderentity WHERE id = :id")
    fun getOrderWithEntitiesById(id: Long): Flow<OrderWithEntities>

    @Query("SELECT * FROM orderentity WHERE id = :id")
    fun getOrderWithEntities(id: Long): OrderWithEntities
}