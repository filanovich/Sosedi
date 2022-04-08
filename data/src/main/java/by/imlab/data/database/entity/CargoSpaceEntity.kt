package by.imlab.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.imlab.data.database.deserializer.DateDeserializer
import by.imlab.data.model.SpaceType
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*

@Entity
data class CargoSpaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val spaceIds: List<Int> = emptyList(),
    val type: SpaceType,
    val barcode: String = "",
    val scanned: Boolean = false,
    @JsonDeserialize(using = DateDeserializer::class)
    val createdAt: Date = Date()
)