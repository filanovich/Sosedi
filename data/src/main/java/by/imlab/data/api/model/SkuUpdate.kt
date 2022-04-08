package by.imlab.data.api.model

import androidx.annotation.Keep
import androidx.room.Entity
import by.imlab.data.model.BarcodeType
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class SkuUpdate(
    @JsonProperty("id")
    val id: Long = 0,
    @JsonProperty("quantity")
    val quantity: Double
)