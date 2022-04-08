package by.imlab.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.database.model.SkuWithEntities
import kotlinx.coroutines.flow.Flow

@Dao
interface SkuDao : BaseDao<SkuEntity> {

    @Query("DELETE FROM skuentity")
    fun deleteAll()

    @Transaction
    @Query("SELECT * FROM skuentity WHERE id = :id")
    fun getSkuWithEntitiesById(id: Long): Flow<SkuWithEntities?>

    @Transaction
    fun moveToLastPosition(sku: SkuEntity) {
        delete(obj = sku)
        insert(obj = sku.copy(id = 0))
    }
}