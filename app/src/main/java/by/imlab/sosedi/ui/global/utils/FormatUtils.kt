package by.imlab.sosedi.ui.global.utils

import android.content.Context
import android.text.Html
import android.text.Spanned
import by.imlab.sosedi.ui.global.extentions.formatTrim

class FormatUtils {
    companion object {
/*        fun formatFromToNumbers(
            context: Context,
            textRes: Int,
            collected: Int,
            total: Int
        ): Spanned {
            val collectedFormatText =
                if (collected < total) "<font color='#FF0505'>$collected</font>"
                else "<font color='#00D358'>$collected</font>"
            val fromThings = context.getString(textRes, "", total.toString())
            return Html.fromHtml(collectedFormatText + fromThings, Html.FROM_HTML_MODE_COMPACT)
        }*/
        fun formatFromToNumbers(
            context: Context,
            textRes: Int,
            collected: Double,
            total: Double
        ): Spanned {
            val decimals = 3
            val sCollected = collected.formatTrim()
            val sTotal = collected.formatTrim()

            val collectedFormatText =
                if (collected < total) "<font color='#FF0505'>$sCollected</font>"
                else "<font color='#00D358'>$sCollected</font>"
            val fromThings = context.getString(textRes, "", sTotal)
            return Html.fromHtml(collectedFormatText + fromThings, Html.FROM_HTML_MODE_COMPACT)
        }
    }
}