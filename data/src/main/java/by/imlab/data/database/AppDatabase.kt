package by.imlab.data.database

import android.content.Context
import androidx.room.*
import by.imlab.data.database.converter.*
import by.imlab.data.database.dao.BarcodeDao
import by.imlab.data.database.dao.CargoSpaceDao
import by.imlab.data.database.dao.OrderDao
import by.imlab.data.database.dao.SkuDao
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.CargoSpaceEntity
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.entity.SkuEntity

@Database(
    entities = [
        OrderEntity::class,
        SkuEntity::class,
        BarcodeEntity::class,
        CargoSpaceEntity::class
               ],
    version = 1,
    exportSchema = true,

//    autoMigrations = [
//        AutoMigration (from = 1, to = 2)
//    ]

)
@TypeConverters(
    DateConverter::class,
    OrderStatusConverter::class,
    BarcodeTypeConverter::class,
    SpaceTypeConverter::class,
    ListIntConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract val orderDao: OrderDao

    abstract val skuDao: SkuDao

    abstract val cargoSpaceDao: CargoSpaceDao

    abstract val barcodeDao: BarcodeDao

    companion object {

        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            //return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
            return Room.databaseBuilder(context, AppDatabase::class.java, "data.db").allowMainThreadQueries().build()
        }

    }

}