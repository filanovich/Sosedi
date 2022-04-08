package by.imlab.data.api.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class Sku(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("orderId")
    val orderId: Long,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("name")
    val name: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("category")
    val category: String,
    @JsonProperty("quantity")
    val quantity: Double,
    @JsonProperty("price")
    val price: Double,
    @JsonProperty("barcodeType")
    val barcodeType: Int,
    @JsonProperty("barcodeId")
    val barcodeId: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image")
    val image: String? = null
) {

//    fun checkBarcodeCorrect(barcodeEntity: BarcodeEntity): Boolean {
//        return barcodeId == barcodeEntity.getCode() && barcodeType == barcodeEntity.getType()
//    }
}