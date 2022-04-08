package by.imlab.data.api.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class OrderResponse (
    @JsonProperty("result")
    var result: Boolean,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("description")
    var description: String? = null,

    @JsonProperty("order")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var orderList: List<Order>? = null,

    @JsonProperty("sku")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var skuList: List<Sku>? = null
)
