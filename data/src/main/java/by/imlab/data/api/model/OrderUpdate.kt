package by.imlab.data.api.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class OrderUpdate(
    @JsonProperty("id")
    val id: Long = 0,
    @JsonProperty("status")
    val status: Int = 0,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("sku")
    var sku: MutableList<SkuUpdate>? = null
)