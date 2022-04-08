package by.imlab.data.database.converter

import androidx.room.TypeConverter
import java.util.*

object DateConverter {

    @TypeConverter
    @JvmStatic
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    @JvmStatic
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}
