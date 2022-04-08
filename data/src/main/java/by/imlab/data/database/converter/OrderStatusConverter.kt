package by.imlab.data.database.converter

import androidx.room.TypeConverter
import by.imlab.data.model.OrderStatus

object OrderStatusConverter {

    @TypeConverter
    @JvmStatic
    fun toOrderStatus(value: Int): OrderStatus = enumValues<OrderStatus>()[value]

    @TypeConverter
    @JvmStatic
    fun fromOrderStatus(orderStatus: OrderStatus): Int {
        return orderStatus.value
    }
}
