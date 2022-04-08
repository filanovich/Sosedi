package by.imlab.data.api.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class ImageResponse (
    @JsonProperty("result")
    var result: Boolean,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("description")
    var description: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image")
    var image: String? = null
)
