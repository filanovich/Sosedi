package by.imlab.data.api.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class SkuId (
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("barcodeId")
    val barcodeId: String
)