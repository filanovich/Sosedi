package by.imlab.data.datasource

import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.model.BarcodeType
import by.imlab.data.model.OrderStatus
import java.util.*

// TODO: Remove context after adding the endpoint
class OrderDataSource {

    private val _orders = listOf(
        OrderEntity(
            number = "1112223333101",
            status = OrderStatus.QUEUE,
            address = "г.Минск, ул. Заречная д.8 кв. 10",
            createdAt = Date(1591047450000)
        ),
        OrderEntity(
            number = "1112223333102",
            status = OrderStatus.QUEUE,
            address = "г.Минск, ул. Заречная д.8 кв. 10",
            createdAt = Date(1591047830000)
        ),
        OrderEntity(
            number = "1112223333103",
            status = OrderStatus.QUEUE,
            address = "г.Минск, ул. Заречная д.8 кв. 10",
            createdAt = Date(1591048085000)
        ),
        OrderEntity(
            number = "1112223333104",
            status = OrderStatus.QUEUE,
            address = "г.Минск, ул. Заречная д.8 кв. 10",
            createdAt = Date(1591048410000)
        ),
        OrderEntity(
            number = "1112223333105",
            status = OrderStatus.QUEUE,
            address = "г.Минск, ул. Заречная д.8 кв. 10",
            createdAt = Date(1591048715000)
        )
    )



//    private val _cargoSpaces = arrayListOf(
//        CargoSpaceEntity(
//            orderId = 1,
//            spaceIds = listOf(1, 2),
//            barcode = "234AB36C74560 (12)",
//            type = SpaceType.CARGO
//        ),
//        CargoSpaceEntity(
//            orderId = 1,
//            spaceIds = listOf(3, 4),
//            barcode = "234AB36C74560 (34)",
//            type = SpaceType.CARGO
//        ),
//        CargoSpaceEntity(
//            orderId = 1,
//            spaceIds = listOf(5),
//            barcode = "5449000091376",
//            type = SpaceType.POCKET
//        )
//    )

    // 5449000091376", "4005900036131

/*    private val _skuList = arrayListOf(
        SkuEntity(
            orderId = 1,
            name = "Чай императорский «Коллекционный молочный улун», 25 п.",
            category = "Бакалея, Чай",
            quantity = 2f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4600288016739"
        ),
        SkuEntity(
            orderId = 1,
            name = "Масло подсолнечное Ricco, 1л",
            category = "Бакалея, Масло",
            quantity = 3f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "46040775035682"
        ),
        SkuEntity(
            orderId = 1,
            name = "Дезодорант-антиперсперант гелевый, Old Spice",
            category = "Косметика, Дезодоранты",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "8001090999153"
        ),
        SkuEntity(
            orderId = 1,
            name = "Крем для волос, Fructis",
            category = "Косметика, Кремы",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "3600542225380"
        ),
        SkuEntity(
            orderId = 1,
            name = "Соль поваренная пищевая выварочная экстра «Полесье»",
            category = "Бакалея, Соль",
            quantity = 4f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4810023003065"
        ),
        SkuEntity(
            orderId = 1,
            name = "Крупа гречневая Ядрица быстроразваривающаяся «Купеческая»",
            category = "Бакалея, Крупы",
            quantity = 3f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4810711001755"
        ),
        SkuEntity(
            orderId = 1,
            name = "Крупа рисовая шлифованная «Рис семейный»",
            category = "Бакалея, Крупы",
            quantity = 2f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4811180000126"
        ),
        SkuEntity(
            orderId = 1,
            name = "Фундук развесной",
            category = "Бакалея, Развесные продукты, Орехи",
            quantity = 3f,
            barcodeType = BarcodeType.CustomWeight,
            barcodeId = "59764"
        ),


        SkuEntity(
            orderId = 2,
            name = "Горох",
            category = "Продукты питания, Крупы",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820045780196"
        ),
        SkuEntity(
            orderId = 2,
            name = "Овощи с рисом \"Дарус\"",
            category = "Продукты питания, Замороженные продукты",
            quantity = 11f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820064480244"
        ),
        SkuEntity(
            orderId = 2,
            name = "Желе \"Клубника\"",
            category = "Продукты питания, Консервированные продукты",
            quantity = 2f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820039290236"
        ),
        SkuEntity(
            orderId = 2,
            name = "Лапша \"Экспресс\" курица",
            category = "Продукты питания, Макаронные изделия",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820048613040"
        ),
        SkuEntity(
            orderId = 2,
            name = "Авокадо",
            category = "Бакалея, Штучный товар маркируемый через весы, Фрукты",
            quantity = 6f,
            barcodeType = BarcodeType.CustomGoods,
            barcodeId = "54961"
        ),


        SkuEntity(
            orderId = 3,
            name = "Дракоша детская гелевая зубная паста со вкусом клубники и кальцием. 60 мл",
            category = "Красота и здоровье, Гигиенические изделия и средства",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4600702023046"
        ),
        SkuEntity(
            orderId = 3,
            name = "Лапша \"Экспресс\" курица",
            category = "Продукты питания, Макаронные изделия",
            quantity = 22f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820048613040"
        ),
        SkuEntity(
            orderId = 3,
            name = "Овощи с рисом \"Дарус\"",
            category = "Продукты питания, Замороженные продукты",
            quantity = 11f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820064480244"
        ),


        SkuEntity(
            orderId = 4,
            name = "Авокадо",
            category = "Бакалея, Штучный товар маркируемый через весы, Фрукты",
            quantity = 99f,
            barcodeType = BarcodeType.CustomGoods,
            barcodeId = "54961"
        ),
        SkuEntity(
            orderId = 4,
            name = "Фундук развесной",
            category = "Бакалея, Развесные продукты, Орехи",
            quantity = 66.999f,
            barcodeType = BarcodeType.CustomWeight,
            barcodeId = "59764"
        ),


        SkuEntity(
            orderId = 5,
            name = "Чай императорский «Коллекционный молочный улун», 25 п.",
            category = "Бакалея, Чай",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4600288016739"
        ),
        SkuEntity(
            orderId = 5,
            name = "Масло подсолнечное Ricco, 1л",
            category = "Бакалея, Масло",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "46040775035682"
        ),
        SkuEntity(
            orderId = 5,
            name = "Дезодорант-антиперсперант гелевый, Old Spice",
            category = "Косметика, Дезодоранты",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "8001090999153"
        ),
        SkuEntity(
            orderId = 5,
            name = "Крем для волос, Fructis",
            category = "Косметика, Кремы",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "3600542225380"
        ),
        SkuEntity(
            orderId = 5,
            name = "Соль поваренная пищевая выварочная экстра «Полесье»",
            category = "Бакалея, Соль",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4810023003065"
        ),
        SkuEntity(
            orderId = 5,
            name = "Крупа гречневая Ядрица быстроразваривающаяся «Купеческая»",
            category = "Бакалея, Крупы",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4810711001755"
        ),
        SkuEntity(
            orderId = 5,
            name = "Крупа рисовая шлифованная «Рис семейный»",
            category = "Бакалея, Крупы",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4811180000126"
        ),
        SkuEntity(
            orderId = 5,
            name = "Горох",
            category = "Продукты питания, Крупы",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820045780196"
        ),
        SkuEntity(
            orderId = 5,
            name = "Овощи с рисом \"Дарус\"",
            category = "Продукты питания, Замороженные продукты",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820064480244"
        ),
        SkuEntity(
            orderId = 5,
            name = "Желе \"Клубника\"",
            category = "Продукты питания, Консервированные продукты",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820039290236"
        ),
        SkuEntity(
            orderId = 5,
            name = "Лапша \"Экспресс\" курица",
            category = "Продукты питания, Макаронные изделия",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820048613040"
        ),
        SkuEntity(
            orderId = 5,
            name = "Дракоша детская гелевая зубная паста со вкусом клубники и кальцием. 60 мл",
            category = "Красота и здоровье, Гигиенические изделия и средства",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4600702023046"
        ),
        SkuEntity(
            orderId = 5,
            name = "Лапша \"Экспресс\" курица",
            category = "Продукты питания, Макаронные изделия",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820048613040"
        ),
        SkuEntity(
            orderId = 5,
            name = "Овощи с рисом \"Дарус\"",
            category = "Продукты питания, Замороженные продукты",
            quantity = 1f,
            barcodeType = BarcodeType.CommonGoods,
            barcodeId = "4820064480244"
        ),
        SkuEntity(
            orderId = 5,
            name = "Авокадо",
            category = "Бакалея, Штучный товар маркируемый через весы, Фрукты",
            quantity = 99f,
            barcodeType = BarcodeType.CustomGoods,
            barcodeId = "54961"
        ),
        SkuEntity(
            orderId = 5,
            name = "Фундук развесной",
            category = "Бакалея, Развесные продукты, Орехи",
            quantity = 69.999f,
            barcodeType = BarcodeType.CustomWeight,
            barcodeId = "59764"
        ),
        SkuEntity(
            orderId = 5,
            name = "Авокадо",
            category = "Бакалея, Штучный товар маркируемый через весы, Фрукты",
            quantity = 6f,
            barcodeType = BarcodeType.CustomGoods,
            barcodeId = "54961"
        )
    )

    fun getOrders() = _orders

    fun getSkuList() = _skuList*/

//    fun getCargoSpaces() = _cargoSpaces
}