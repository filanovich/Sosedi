package by.imlab.data.database.converter

import androidx.room.TypeConverter
import by.imlab.data.model.BarcodeType

object BarcodeTypeConverter {

    @TypeConverter
    @JvmStatic
    fun toBarcodeType(value: Int): BarcodeType = enumValues<BarcodeType>()[value]

    @TypeConverter
    @JvmStatic
    fun fromBarcodeType(barcodeType: BarcodeType): Int {
        return barcodeType.value
    }
}
