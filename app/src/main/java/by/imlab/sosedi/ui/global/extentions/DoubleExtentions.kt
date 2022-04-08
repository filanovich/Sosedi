package by.imlab.sosedi.ui.global.extentions

fun Double.formatTrim() = "%.3f".format(this).trimEnd('0').trimEnd(',').trimEnd('.')

fun Double.formatValue(decimals: Int = 2) : String {
    var result = "%.${decimals}f".format(this).trimEnd('0')
    if (result.endsWith(".") || result.endsWith(","))
        result += "0";
    return result
}
