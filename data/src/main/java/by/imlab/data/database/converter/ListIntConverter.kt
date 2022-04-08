package by.imlab.data.database.converter

import androidx.room.TypeConverter

object ListIntConverter {

    @TypeConverter
    @JvmStatic
    fun toListInt(value: String): List<Int> {
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    @JvmStatic
    fun fromListInt(list: List<Int>): String {
        return list.joinToString(",")
    }
}
