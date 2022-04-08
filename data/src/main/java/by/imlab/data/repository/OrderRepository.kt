package by.imlab.data.repository

import by.imlab.core.extensions.set
import by.imlab.data.api.APIService
import by.imlab.data.api.Common
import by.imlab.data.api.model.*
import by.imlab.data.database.AppDatabase
import by.imlab.data.database.converter.BarcodeTypeConverter
import by.imlab.data.database.converter.OrderStatusConverter
import by.imlab.data.database.dao.CargoSpaceDao
import by.imlab.data.database.dao.OrderDao
import by.imlab.data.database.dao.SkuDao
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.datasource.OrderDataSource
import by.imlab.data.model.OrderStatus
import by.imlab.data.model.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class OrderRepository(
    private val orderDataSource: OrderDataSource,
    private val cargoSpaceDao: CargoSpaceDao,
    //private val appDb: AppDatabase,
    private val orderDao: OrderDao,
    private val skuDao: SkuDao,
    private val loginRepository: LoginRepository,
    private val barcodeRepository: BarcodeRepository,
    private val cargoSpaceRepository: CargoSpaceRepository,
    private val common: Common
    ) {

    fun deleteAll() = orderDao.deleteAll()

    // Orders list
    fun fetchOrdersListWithEntities() = orderDao.getOrdersWithEntities()

    // Orders list
    fun fetchOrdersList() = orderDao.getOrders()

    // Order by id
    fun fetchOrderById(id: Long) = orderDao.getOrderById(id = id)

    // Order by number
    fun fetchOrderByNumber(number: String) = orderDao.getOrderByNumber(number = number)

    fun fetchOrderWithEntitiesById(id: Long) = orderDao.getOrderWithEntitiesById(id = id)
    fun fetchOrderWithEntities(id: Long) = orderDao.getOrderWithEntities(id = id)

    // Order by status
    fun fetchOrderByStatus(status: OrderStatus) =
        orderDao.getOrderByStatus(status = status).distinctUntilChanged()

    // Order with entities by status
    fun fetchOrderWithEntitiesByStatus(status: OrderStatus) =
        orderDao.getOrderWithEntitiesByStatus(status = status).distinctUntilChanged()

    // Update order
    fun updateOrder(order: OrderEntity) = orderDao.update(order)

    fun updateOrder(order: OrderEntity, callback: (result: Result) -> Unit) {

        val service: APIService = common.retrofitService

        var orderUpdate: OrderUpdate = OrderUpdate(order.id, OrderStatusConverter.fromOrderStatus(order.status))
        if (order.status == OrderStatus.COLLECTION) {
            val orderWithEntities: OrderWithEntities = fetchOrderWithEntities(order.id)

            orderUpdate.sku = mutableListOf()

            orderWithEntities.skuList.forEach {
                orderUpdate.sku!!.add(
                    SkuUpdate(
                        id = it.sku.id,
                        quantity = it.getCollected()
                    )
                )
            }
        }

        var token: String? = ""
        if (loginRepository.isLoggedIn)
            token = loginRepository.user?.token

        service.updateOrder("Picker.updateOrder", token, orderUpdate).enqueue(object : Callback<OrderUpdateResponse> {

            override fun onFailure(call: Call<OrderUpdateResponse>, t: Throwable) {
                callback.invoke(Result.Error(t.message));
            }
            override fun onResponse(call: Call<OrderUpdateResponse>, response: Response<OrderUpdateResponse>) {
                if (!response.isSuccessful) {
                    callback.invoke(Result.Error(response.message()));
                } else {
                    val resp: OrderUpdateResponse = response.body() as OrderUpdateResponse

                    if (resp.result)
                        callback.invoke(Result.Success<Any>(null));
                    else
                        callback.invoke(Result.Error(resp.description));
                }
            }
        })
    }

    fun getOrders(callback: (result: String?) -> Unit) {

//        orderDao.deleteAll()
//        skuDao.deleteAll()

        val service: APIService = common.retrofitService

        var token: String? = ""
        if (loginRepository.isLoggedIn)
            token = loginRepository.user?.token

        service.getOrders("Picker.getOrders", token).enqueue(object : Callback<OrderResponse> {
            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                callback.invoke(t.message);
            }
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {

                if (!response.isSuccessful) {
                    callback.invoke(response.errorBody().toString());
                } else {

                    //appDb.beginTransaction()

                    orderDao.deleteAll()
                    skuDao.deleteAll()

                    val resp: OrderResponse = response.body() as OrderResponse

                    if (resp.orderList != null && resp.skuList != null) {

                        val orderList: List<Order> = response.body()?.orderList as List<Order>
                        val skuList: List<Sku> = response.body()?.skuList as List<Sku>

                        if (skuList != null) {
                            var sku: MutableList<SkuEntity> = mutableListOf()
                            skuList.forEach {
                                //val bt: BarcodeType = BarcodeType.valueOf(it.barcodeType)!!
                                val skuId: Long = it.id
                                //barcodeRepository.resetScannedCodes(skuId);
                                sku.add(
                                    SkuEntity(
                                        id = it.id,
                                        orderId = it.orderId,
                                        name = it.name,
                                        category = it.category,
                                        quantity = it.quantity,
                                        price = it.price,
                                        barcodeType = BarcodeTypeConverter.toBarcodeType(it.barcodeType),
                                        barcodeId = it.barcodeId,
                                        //image = "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"
                                        image = it.image ?: ""
                                    )
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    fetchOrderById(id = it.orderId).catch {
                                    }.collect { order ->
                                        if (order != null && order.status == OrderStatus.QUEUE) {
                                            barcodeRepository.resetScannedCodes(skuId)
                                            cargoSpaceRepository.resetSpacesByOrderId(order.id)
                                        }
                                    }

                                }
                            }

                            //CoroutineScope(Dispatchers.IO).launch {
                                skuDao.insert(sku)
                            //}

                        }

                        if (orderList != null) {
                            var orders: MutableList<OrderEntity> = mutableListOf()
                            orderList.forEach {
                                orders.add(
                                    OrderEntity(
                                        it.id,
                                        it.number,
                                        OrderStatusConverter.toOrderStatus(it.status),
                                        it.address,
                                        Date(it.createdAt)
                                    )
                                )
                            }

                            //CoroutineScope(Dispatchers.IO).launch {
                                orderDao.insert(orders)
                            //}
                        }

                        callback.invoke(null);
                    } else {
                        callback.invoke(null);
                    }
                }
            }
        })
    }

    fun getStockBalance(sku: SkuEntity, callback: (result: Result) -> Unit) {

        val service: APIService = common.retrofitService

        val token = if (loginRepository.isLoggedIn) loginRepository.user?.token else ""

        service.getStockBalance("Picker.getStockBalance", token, SkuId(id = sku.id, barcodeId = sku.barcodeId)).enqueue(object : Callback<StockBalanceResponse> {

            override fun onFailure(call: Call<StockBalanceResponse>, t: Throwable) {
                callback.invoke(Result.Error(t.message));
            }
            override fun onResponse(call: Call<StockBalanceResponse>, response: Response<StockBalanceResponse>) {
                if (!response.isSuccessful) {
                    callback.invoke(Result.Error(response.message()));
                } else {
                    val resp: StockBalanceResponse = response.body() as StockBalanceResponse

                    if (resp.result)
                        callback.invoke(Result.Success(resp.value));
                    else
                        callback.invoke(Result.Error(resp.description));
                }
            }
        })
    }

    fun getImage(sku: SkuEntity, callback: (result: Result) -> Unit) {

        val service: APIService = common.retrofitService

        val token = if (loginRepository.isLoggedIn) loginRepository.user?.token else ""

        service.getImage("Picker.getImage", token, SkuId(id = sku.id, barcodeId = sku.barcodeId)).enqueue(object : Callback<ImageResponse> {

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                callback.invoke(Result.Error(t.message));
            }
            override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                if (!response.isSuccessful) {
                    callback.invoke(Result.Error(response.message()));
                } else {
                    val resp: ImageResponse = response.body() as ImageResponse

                    if (resp.result)
                        callback.invoke(Result.Success(resp.image));
                    else
                        callback.invoke(Result.Error(resp.description));
                }
            }
        })
    }

    fun checkNewOrders(callback: (result: Boolean) -> Unit) {

        val service: APIService = common.retrofitService

        var token: String? = ""
        if (loginRepository.isLoggedIn)
            token = loginRepository.user?.token

        service.checkNewOrder("Picker.checkNewOrders", token).enqueue(object : Callback<by.imlab.data.api.model.Response> {
            override fun onFailure(call: Call<by.imlab.data.api.model.Response>, t: Throwable) {
                callback.invoke(false);
            }
            override fun onResponse(call: Call<by.imlab.data.api.model.Response>, response: Response<by.imlab.data.api.model.Response>) {

                if (!response.isSuccessful) {
                    callback.invoke(false);
                } else {
                    val resp: by.imlab.data.api.model.Response = response.body() as by.imlab.data.api.model.Response
                    callback.invoke(resp.result);
                }
            }
        })
    }

    // TODO: Remove this
    init {

//        val service: APIService = Common.retrofitService

//        service.getOrders(
//            "Picker.getOrders"
//        ).enqueue(object : Callback<MutableList<OrderEntity>> {

//            override fun onFailure(call: Call<MutableList<OrderEntity>>, t: Throwable) {
//            }

//            override fun onResponse(call: Call<MutableList<OrderEntity>>, response: Response<MutableList<OrderEntity>>) {
//                if (response.isSuccessful) {
//                    val orders: MutableList<OrderEntity> = response.body() as MutableList<OrderEntity>
//                    orderDao.insert(orders)

                    //val m: LinkedTreeMap<String, String> = resp.data as LinkedTreeMap<String, String>
                    //val user = User(m["userId"], m["displayName"])
                    //setLoggedInUser(user)
                    //callback.invoke(Result.Success(user))
//                }
//            }
//        })

//        val service: APIService = Common.retrofitService
//        val responseCall: Call<MutableList<OrderEntity>> = service.getOrders("Picker.getOrders");
//        val response: Response<MutableList<OrderEntity>> = responseCall.execute();
//        if (response.isSuccessful) {
//            val orders: MutableList<OrderEntity> = response.body() as MutableList<OrderEntity>
//            orderDao.insert(orders)
//        }

        CoroutineScope(Dispatchers.IO).launch {

//            val orders = orderDataSource.getOrders()
//            orderDao.insert(orders)
//            val skuList = orderDataSource.getSkuList()
//            skuDao.insert(skuList)
//            val cargoList = orderDataSource.getCargoSpaces()
//            cargoSpaceDao.insert(cargoList)
        }
    }

}