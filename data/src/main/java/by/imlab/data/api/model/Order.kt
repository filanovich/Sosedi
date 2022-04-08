package by.imlab.data.api.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class Order(
    @JsonProperty("id")
    val id: Long = 0,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("number")
    val number: String,

    @JsonProperty("status")
    val status: Int = 0,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("address")
    val address: String,

    @JsonProperty("createdAt")
    val createdAt: Long = 0
) {
//    fun getFormatDate(): String {
//        val formatter = SimpleDateFormat("dd.MM.yy hh:mm:ss", Locale.ROOT)
//        return formatter.format(createdAt)
//    }
}