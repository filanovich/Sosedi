package by.imlab.sosedi.ui.global.print

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer

sealed class PrintFormat {

    data class Text(
        val text: String = "",
        val textSize: Int = 28,
        val bold: Boolean = false,
        val align: Paint.Align = Paint.Align.LEFT,
        val bottomMargin: Int = 8,
        val underline: Boolean = false,
    ) : PrintFormat()

    data class Quantity(
        val info: String = "",
        val value: String = "",
        val bottomMargin: Int = 8
    ) : PrintFormat() {
        fun getInfoPaint(): Paint {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.isAntiAlias = true
            paint.textSize = 24f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            return paint
        }

        fun getValuePaint(): Paint {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.isAntiAlias = true
            paint.textSize = 30f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            return paint
        }

        fun getHeight(): Int {
            val paint = this.getValuePaint()
            val baseline = -paint.ascent()
            return (baseline + paint.descent() + bottomMargin).toInt()
        }
    }

    data class Field(
        val text: String = "",
        val textSize: Float = 24f,
        val textAlign: Paint.Align = Paint.Align.LEFT,
        val style: Int = Typeface.NORMAL,
        val x: Float = 0f
    ) {
        fun getPaint(): Paint {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.isAntiAlias = true
            paint.textSize = textSize
            paint.color = Color.BLACK
            paint.textAlign = textAlign
            paint.typeface = Typeface.create(Typeface.DEFAULT, style)
            return paint
        }
    }

    data class Detail(
        val a: ArrayList<Field>,
        val bottomMargin: Int = 8
    ) : PrintFormat() {

        fun getHeight(): Int {
            val paint = a.first().getPaint()
            val baseline = -paint.ascent()
            return (baseline + paint.descent() + bottomMargin).toInt()
        }
    }

    data class Indent(val height: Int) : PrintFormat()

    data class Barcode(
        val barcodeValue: String,
        val bottomMargin: Int = 20
    ) : PrintFormat() {
        private val bitMatrix: BitMatrix by lazy { generateBitMatrix() }

        fun getHeight(): Int {
            return BARCODE_HEIGHT
        }

        fun getWidth(): Int {
            return BARCODE_WIDTH
        }

        fun getPixels(): IntArray {
            val barcodeColor = Color.BLACK
            val backgroundColor = Color.WHITE
            val height = bitMatrix.height
            val width = bitMatrix.width

            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] =
                        if (bitMatrix.get(x, y)) barcodeColor else backgroundColor
                }
            }
            return pixels
        }

        private fun generateBitMatrix(): BitMatrix {
            return Code128Writer().encode(
                barcodeValue,
                BarcodeFormat.CODE_128,
                BARCODE_WIDTH,
                BARCODE_HEIGHT
            )
        }

        companion object {
            private const val BARCODE_HEIGHT = 80
            private const val BARCODE_WIDTH = PrintBitmap.LABEL_WIDTH
        }
    }
}

fun PrintFormat.Text.getPaint(): Paint {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.isAntiAlias = true
    paint.textSize = textSize.toFloat()
    paint.color = Color.BLACK
    paint.textAlign = align
    paint.typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    return paint
}

fun PrintFormat.Text.getHeight(): Int {
    val paint = this.getPaint()
    val baseline = -paint.ascent()
    return (baseline + paint.descent() + bottomMargin).toInt()
}