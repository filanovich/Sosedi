package by.imlab.sosedi.ui.global.print

import android.content.Context
import android.graphics.*
import by.imlab.data.database.entity.OrderEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.formatTrim
import by.imlab.sosedi.ui.global.extentions.formatValue

object PrintBitmap {
    const val LABEL_WIDTH = 576

    fun generateSpecificationLabel(context: Context, orderWithEntities: OrderWithEntities): Bitmap {
        val order = orderWithEntities.order
        val skuList = orderWithEntities.skuList
        val orderCreated = addInToDate(order.getFormatDate())
        val orderCollected = addInToDate(order.getFormatCollectDate())
        val spacesNumber = orderWithEntities.cargoSpaceList.sumOf { it.spaceIds.size }.toString()
        val fullyPackedSku = context.getString(
            R.string.from_things,
            orderWithEntities.getCollectedSkuCount().toString(),
            orderWithEntities.skuList.size.toString()
        )
        val totalPackedProducts = context.getString(
            R.string.from_things,
            orderWithEntities.getCollectedGoodsCount().formatTrim(),
            orderWithEntities.getTotalGoods().formatTrim()
        )

        //val label = arrayListOf<PrintFormat>()

//        label.add(PrintFormat.Indent(height = 120))

        val label = arrayListOf(
            PrintFormat.Indent(height = 120),

            PrintFormat.Text(text = "Номер заказа", textSize = 24),
            PrintFormat.Text(
                text = "№ ${order.number}",
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Заказ открыт", textSize = 24),
            PrintFormat.Text(
                text = orderCreated,
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Заказ собран", textSize = 24),
            PrintFormat.Text(
                text = orderCollected,
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Адрес заказа", textSize = 24),
            PrintFormat.Text(
                text = order.address,
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Кол-во пакетов/грузовых мест", textSize = 24),
            PrintFormat.Text(
                text = spacesNumber,
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Полностью собрано SKU", textSize = 24),
            PrintFormat.Text(
                text = fullyPackedSku,
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Всего собрано товаров", textSize = 24),
            PrintFormat.Text(
                text = totalPackedProducts,
                textSize = 30,
                bold = true,
                bottomMargin = 60
            )
        )

        label.add(
            PrintFormat.Text(
                text = "СПЕЦИФИКАЦИЯ ЗАКАЗА",
                textSize = 24,
                align = Paint.Align.CENTER,
                bottomMargin = 25
                //underline = true
            )
        )

        val header = arrayListOf<PrintFormat.Field>(
            PrintFormat.Field(text = "Наименование/штрихкод", textAlign = Paint.Align.LEFT),
            PrintFormat.Field(text = "Кол.", x = 400f, textAlign = Paint.Align.RIGHT),
            PrintFormat.Field(text = "Цена", x = 470f, textAlign = Paint.Align.RIGHT),
            PrintFormat.Field(text = "Сумма", x = LABEL_WIDTH.toFloat(), textAlign = Paint.Align.RIGHT)
        )

        label.add(PrintFormat.Detail(a = header))

        var sumValue = 0.0

        for (skuWithEntities in skuList) {

            val sku = skuWithEntities.sku

            label.add(
                PrintFormat.Text(
                    text = sku.name.uppercase(),
                    textSize = 22,
                    bottomMargin = 4
                )
            )

            val collected = skuWithEntities.getCollected()
            val price = sku.price
            val value = collected * price

            sumValue += value

            val detail = arrayListOf<PrintFormat.Field>(
                PrintFormat.Field(
                    text = sku.barcodeId,
                    textSize = 22f,
                    textAlign = Paint.Align.LEFT
                ),
                PrintFormat.Field(
                    text = collected.formatValue(3),
                    x = 400f,
                    textSize = 22f,
                    textAlign = Paint.Align.RIGHT
                ),
                PrintFormat.Field(
                    text = price.formatValue(2),
                    x = 470f,
                    textSize = 22f,
                    textAlign = Paint.Align.RIGHT
                ),
                PrintFormat.Field(
                    text = value.formatValue(2),
                    x = LABEL_WIDTH.toFloat(),
                    textSize = 22f,
                    textAlign = Paint.Align.RIGHT)
            )

            label.add(PrintFormat.Detail(a = detail, bottomMargin = 4))
        }

        label.add(PrintFormat.Indent(10))

        val sumQuant = skuList.sumOf { it.getCollected() }

        val footer = arrayListOf<PrintFormat.Field>(
            PrintFormat.Field(
                text = "ИТОГО:",
                textSize = 28f,
                textAlign = Paint.Align.LEFT,
                style = Typeface.BOLD
            ),
            PrintFormat.Field(
                text = sumQuant.formatValue(3),
                x = 400f,
                textSize = 28f,
                textAlign = Paint.Align.RIGHT,
                style = Typeface.BOLD
            ),
            PrintFormat.Field(
                text = sumValue.formatValue(2),
                x = LABEL_WIDTH.toFloat(),
                textSize = 28f,
                textAlign = Paint.Align.RIGHT,
                style = Typeface.BOLD
            )
        )
        label.add(PrintFormat.Detail(a = footer))

        label.add(PrintFormat.Indent(20))

        label.add(
            PrintFormat.Text(
                text = "НЕ ЯВЛЯЕТСЯ ФИСКАЛЬНЫМ ДОКУМЕНТОМ",
                textSize = 24,
                bold = true,
                bottomMargin = 20,
                align = Paint.Align.CENTER
            )
        )

        var barcode = order.number.filter { it.isLetterOrDigit() }
        label.add(PrintFormat.Barcode(barcodeValue = barcode))
        //label.add(PrintFormat.Barcode(barcodeValue = order.number))
        label.add(PrintFormat.Text(text = order.number, textSize = 20, align = Paint.Align.CENTER))

        return generate(label)
    }

    fun generateCargoSpaceLabel(order: OrderEntity, spacesIds: List<Int>): Bitmap {
        val commaSeparatedIds = spacesIds.joinToString(",")
        val noSeparatedIds = spacesIds.joinToString("")

        val label = arrayListOf(
            PrintFormat.Indent(height = 120),

            PrintFormat.Text(text = "Номер заказа", textSize = 24),
            PrintFormat.Text(
                text = "№ ${order.number}",
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Адрес заказа", textSize = 24),
            PrintFormat.Text(
                text = order.address,
                textSize = 30,
                bold = true,
                bottomMargin = 15
            ),

            PrintFormat.Text(text = "Номер пакета в заказе/номер грузового места", textSize = 24)
        )

        label.addAll(
            formatTextMultiline(
                text = "№ $commaSeparatedIds",
                textSize = 30,
                bold = true,
                bottomMargin = 15,
                delimiters = ","
            )
        )

        //label.add(PrintFormat.Barcode(barcodeValue = "${order.number} ($noSeparatedIds)"))
        var barcode = order.number.filter { it.isLetterOrDigit() }
        label.add(PrintFormat.Barcode(barcodeValue = "${barcode}$noSeparatedIds"))
        label.add(
            PrintFormat.Text(
                text = "${order.number} ($noSeparatedIds)",
                textSize = 20,
                align = Paint.Align.CENTER
            )
        )

        return generate(label)
    }

    private fun generate(label: List<PrintFormat>): Bitmap {
        var height = 0
        label.forEach {
            height += when (it) {
                is PrintFormat.Text -> it.getHeight()
                is PrintFormat.Indent -> it.height
                is PrintFormat.Barcode -> it.getHeight() + it.bottomMargin
                is PrintFormat.Quantity -> it.getHeight() + it.bottomMargin
                is PrintFormat.Detail -> it.getHeight() + it.bottomMargin
            }
        }

        // Create bitmap and canvas to draw to
        val image = Bitmap.createBitmap(LABEL_WIDTH, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(image)

        // Draw background
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawPaint(paint)

        // Draw texts
        canvas.save()
        canvas.translate(0.0f, 0.0f)

        var lastItemY = 0f

        label.forEach {

            when (it) {

                is PrintFormat.Text -> {

                    canvas.drawText(
                        it.text,
                        if (it.align == Paint.Align.CENTER) (LABEL_WIDTH / 2).toFloat() else 0f,
                        lastItemY,
                        it.getPaint()
                    )

                    if (it.underline) {
                        canvas.drawLine(
                            (LABEL_WIDTH * 0.25).toFloat(),
                            lastItemY + 8,
                            (LABEL_WIDTH * 0.75).toFloat(),
                            lastItemY + 8,
                            Paint()
                        )
                    }
                    lastItemY += it.getHeight()
                }

                is PrintFormat.Quantity -> {
                    canvas.drawText(
                        it.info,
                        0f,
                        lastItemY,
                        it.getInfoPaint()
                    )
                    canvas.drawText(
                        it.value,
                        LABEL_WIDTH - it.getValuePaint().strokeWidth,
                        lastItemY,
                        it.getValuePaint()
                    )
                    lastItemY += it.getHeight()
                }

                is PrintFormat.Detail -> {

                    it.a.forEach {
                        canvas.drawText(
                            it.text,
                            it.x,
                            lastItemY,
                            it.getPaint()
                        )
                    }

                    lastItemY += it.getHeight()

                }

                is PrintFormat.Indent -> {
                    lastItemY += it.height
                }

                is PrintFormat.Barcode -> {
                    image.setPixels(
                        it.getPixels(),
                        0,
                        it.getWidth(),
                        0,
                        lastItemY.toInt(),
                        it.getWidth(),
                        it.getHeight()
                    )
                    lastItemY += it.getHeight() + it.bottomMargin
                }
            }
        }

        canvas.restore()
        return image
    }

    private fun addInToDate(formatDate: String): String {
        return formatDate.replace(" ", " в ", true)
    }

    private fun formatTextMultiline(
        text: String,
        textSize: Int = 24,
        bold: Boolean = false,
        bottomMargin: Int = 8,
        delimiters: String = " "
    ): List<PrintFormat> {
        val textList = arrayListOf<PrintFormat>()
        val splitText = text.split(delimiters)

        var textLine = ""
        for (word in splitText) {
            if (textLine.plus(word).length < 44) {
                textLine = textLine.plus(word).plus(delimiters)
            } else {
                textList.add(
                    PrintFormat.Text(
                        text = textLine,
                        textSize = textSize,
                        bold = bold
                    )
                )
                textLine = ""
                textLine = textLine.plus(word).plus(delimiters)
            }
            if (word == splitText.last()) {
                textList.add(
                    PrintFormat.Text(
                        text = textLine,
                        textSize = textSize,
                        bold = bold,
                        bottomMargin = bottomMargin
                    )
                )
            }
        }
        return textList
    }
}