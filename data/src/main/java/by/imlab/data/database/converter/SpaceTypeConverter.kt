package by.imlab.data.database.converter

import androidx.room.TypeConverter
import by.imlab.data.model.SpaceType

object SpaceTypeConverter {

    @TypeConverter
    @JvmStatic
    fun toSpaceType(value: Int): SpaceType = enumValues<SpaceType>()[value]

    @TypeConverter
    @JvmStatic
    fun fromSpaceType(spaceType: SpaceType): Int {
        return spaceType.value
    }
}
