package by.imlab.data.database.entity

import android.icu.text.SimpleDateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import by.imlab.data.database.deserializer.DateDeserializer
import by.imlab.data.model.OrderStatus
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*

@Entity
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var number: String,
    var status: OrderStatus,
    var address: String,
    @JsonDeserialize(using = DateDeserializer::class)
    var createdAt: Date = Date(),
    @JsonDeserialize(using = DateDeserializer::class)
    val collectedAt: Date = Date(),
    @JsonDeserialize(using = DateDeserializer::class)
    val transferredAt: Date = Date()
) {
    fun getFormatDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yy hh:mm:ss", Locale.ROOT)
        return formatter.format(createdAt)
    }

    fun getFormatCollectDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yy hh:mm:ss", Locale.ROOT)
        return formatter.format(collectedAt)
    }

    fun getFormatTransferDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yy hh:mm:ss", Locale.ROOT)
        return formatter.format(transferredAt)
    }
}