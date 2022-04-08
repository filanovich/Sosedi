package by.imlab.data.api

import by.imlab.data.api.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIService {

    @POST("/exec")
    fun authentication(
        @Query("action") action: String?,
        @Body login: Login
    ): Call<LoginResponse>

    @POST("/exec")
    fun logout(
        @Query("action") action: String?,
        @Query("p") token: String?
    ): Call<Void>

    @GET("/exec")
    fun getOrders(
        @Query("action") action: String?,
        @Query("p") token: String?
    ): Call<OrderResponse>

    //@FormUrlEncoded
    @POST("/exec")
    fun updateOrder(
        @Query("action") action: String?,
        @Query("p") token: String?,
        @Body order: OrderUpdate
    ): Call<OrderUpdateResponse>

    @POST("/exec")
    fun getStockBalance(
        @Query("action") action: String?,
        @Query("p") token: String?,
        @Body sku: SkuId
    ): Call<StockBalanceResponse>

    @POST("/exec")
    fun getImage(
        @Query("action") action: String?,
        @Query("p") token: String?,
        @Body sku: SkuId
    ): Call<ImageResponse>

    @GET("/exec")
    fun checkNewOrder(
        @Query("action") action: String?,
        @Query("p") token: String?,
    ): Call<Response>


}