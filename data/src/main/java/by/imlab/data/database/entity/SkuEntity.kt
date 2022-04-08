package by.imlab.data.database.entity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import by.imlab.data.model.BarcodeType

@Entity
data class SkuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val name: String,
    val category: String,
    val quantity: Double,
    val price: Double = 0.0,
    val barcodeType: BarcodeType,
    val barcodeId: String,
    val image: String
) {

    fun checkBarcodeCorrect(barcodeEntity: BarcodeEntity): Boolean {
        return barcodeId == barcodeEntity.getCode() && barcodeType == barcodeEntity.getType()
    }

    fun convertToImage(): Bitmap {
        val decodedString = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}