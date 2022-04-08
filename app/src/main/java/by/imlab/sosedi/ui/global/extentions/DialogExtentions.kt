package by.imlab.sosedi.ui.global.extentions

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputFilter
import android.text.InputType
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.imlab.data.database.entity.BarcodeEntity
import by.imlab.data.database.entity.SkuEntity
import by.imlab.data.database.model.OrderWithEntities
import by.imlab.data.database.model.SkuWithEntities
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.helpers.PrinterBarcode
import by.imlab.sosedi.ui.global.inputfilters.DoubleInputFilter
import by.imlab.sosedi.ui.global.views.ScannedBarcodeView
import by.imlab.sosedi.ui.global.views.SpacesCounterView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.common_counter_view.view.*
import java.util.*


fun Fragment.dialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes positiveText: Int,
    positive: () -> Unit
) {
    return dialog(getString(title), getString(message), getString(positiveText), positive)
}

fun Fragment.dialog(@StringRes message: Int, @StringRes positiveText: Int, positive: () -> Unit) {
    return dialog(getString(message), getString(positiveText), positive)
}

fun Fragment.dialog(message: String, positiveText: String, positive: () -> Unit) {
    return dialog("", message, positiveText, positive)
}

fun Fragment.dialog(title: String, message: String, positiveText: String, positive: () -> Unit) {
    return dialog(title, message, positiveText, positive, "", {})
}

fun Fragment.dialog(
    title: String,
    message: String,
    positiveText: String,
    positive: () -> Unit,
    negativeText: String,
    negative: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    if (title.isNotEmpty()) {
        if (title.length > 30) {
            val customTitle = TextView(this.requireContext())
            customTitle.text = title
            customTitle.textSize = 17f
            customTitle.setPadding(25, 25, 25, 0)
            customTitle.setTypeface(null, Typeface.BOLD)
            customTitle.setTextColor(Color.BLACK)
            builder.setCustomTitle(customTitle)
        } else {
            builder.setTitle(title)
        }
    }
    builder.setCancelable(false)
    builder.setMessage(message)
    builder.setPositiveButton(positiveText.uppercase(Locale.ROOT))
    { _, _ -> positive() }
    if (negativeText.isNotEmpty()) {
        builder.setNegativeButton(negativeText.uppercase(Locale.ROOT))
        { _, _ -> negative() }
    }
    return builder.create().show()
}

fun Fragment.dialog(
    @StringRes message: Int,
    @StringRes positiveText: Int, positive: () -> Unit,
    @StringRes negativeText: Int, negative: () -> Unit
) {
    return dialog(
        getString(message),
        getString(positiveText),
        positive,
        getString(negativeText),
        negative
    )
}

fun AppCompatActivity.dialog(
    @StringRes message: Int,
    @StringRes positiveText: Int, positive: () -> Unit,
    @StringRes negativeText: Int, negative: () -> Unit
) {
    return dialog(
        getString(message),
        getString(positiveText),
        positive,
        getString(negativeText),
        negative
    )
}

fun Fragment.dialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes positiveText: Int, positive: () -> Unit,
    @StringRes negativeText: Int, negative: () -> Unit
) {
    return dialog(
        getString(title),
        getString(message),
        getString(positiveText),
        positive,
        getString(negativeText),
        negative
    )
}

fun AppCompatActivity.dialog(
    message: String,
    positiveText: String, positive: () -> Unit,
    negativeText: String, negative: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
    builder.setCancelable(false)
    builder.setMessage(message)
        .setPositiveButton(positiveText.uppercase()) { _, _ -> positive() }
        .setNegativeButton(negativeText.uppercase()) { _, _ -> negative() }
    return builder.create().show()
}

fun AppCompatActivity.dialog(
    message: String,
    positiveText: String, positive: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
    builder.setCancelable(false)
    builder.setMessage(message)
        .setPositiveButton(positiveText.uppercase()) { _, _ -> positive() }
    return builder.create().show()
}

fun Fragment.dialog(
    message: String,
    positiveText: String, positive: () -> Unit,
    negativeText: String, negative: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    builder.setCancelable(false)
    builder.setMessage(message)
        .setPositiveButton(positiveText.uppercase()) { _, _ -> positive() }
        .setNegativeButton(negativeText.uppercase()) { _, _ -> negative() }
    return builder.create().show()
}

fun Fragment.dialogWithInputBarcode(
    @StringRes title: Int = R.string.enter_product_code,
    @StringRes positiveText: Int = R.string.next,
    positive: (code: PrinterBarcode) -> Unit,
    @StringRes negativeText: Int = R.string.cancel,
    negative: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    builder.setTitle(title)
    builder.setCancelable(false)

    // Add input field to dialog
    val viewInflater = LayoutInflater.from(this.context)
        .inflate(R.layout.dialog_input_layout, this.requireView() as ViewGroup, false)
    val input = viewInflater.findViewById<TextInputEditText>(R.id.codeInput)
    builder.setView(viewInflater)

    // Buttons listeners
    builder.setPositiveButton(getString(positiveText).uppercase()) { _, _ ->
        positive(PrinterBarcode(input.text.toString()))
    }
    builder.setNegativeButton(getString(negativeText).uppercase()) { _, _ -> negative() }

    // Create and show dialog
    return builder.create().show()
}

fun FrameLayout.dialogWithInputWeight(
    context: Fragment,
    sku: SkuEntity,
    @StringRes positiveText: Int = R.string.next,
    positive: (weight: Double) -> Unit
) {
    val builder = MaterialAlertDialogBuilder(context.requireContext(), R.style.AlertDialogTheme)
    builder.setTitle(R.string.enter_weight_code)
    builder.setCancelable(false)

    // Add input field to dialog
    val viewInflater = LayoutInflater.from(context.requireContext())
        .inflate(R.layout.dialog_weight_layout, context.requireView() as ViewGroup, false)
    val input = viewInflater.findViewById<TextInputEditText>(R.id.weightInput)
    input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    input.setFilters(arrayOf<InputFilter>(DoubleInputFilter(0.0, 99.999, 3)))
    input.requestFocus()

    builder.setView(viewInflater)


    //Buttons listeners
    builder.setPositiveButton(context.getString(positiveText).uppercase()) { _, _ ->
        var weight = 0.0
        try {
            weight = input.text.toString().toDouble()
        } catch (e: Exception) {}

        positive(weight)
    }
    builder.setNegativeButton(context.getString(R.string.cancel).uppercase(), null)

    // Create and show dialog
    return builder.create().show()
}

fun Fragment.dialogWithBarcodeList(
    sku: SkuEntity,
    barcodeList: MutableList<BarcodeEntity>,
    @StringRes positiveText: Int,
    positive: (MutableList<BarcodeEntity>) -> Unit
) {
    val localList = arrayListOf<BarcodeEntity>()
    localList.addAll(barcodeList)

    // Alert dialog builder
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    builder.setCancelable(false)

    // Set view
    val scannedView = ScannedBarcodeView(
        this,
        sku,
        barcodeList = barcodeList,
        onDropListener = {barcodeEntity -> localList.remove(barcodeEntity) },
        onNewListener = {barcodeEntity -> localList.add(barcodeEntity) }
    )

    builder.setView(scannedView)

    // Buttons listeners
    builder.setPositiveButton(getString(positiveText).uppercase()) { _, _ ->
        positive(localList)
    }

    // Create and show dialog
    return builder.create().show()
}

fun Fragment.dialogWithCounter(
    @StringRes title: Int,
    @StringRes positiveText: Int,
    positive: (Int) -> Unit,
    @StringRes negativeText: Int,
    negative: () -> Unit,
    allowedSpaces: Int
) {
    // Alert dialog builder
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    builder.setTitle(title)
    builder.setCancelable(false)

    // Set view
    val counterView = SpacesCounterView(context = requireContext())
    counterView.indicateQuantityText.isVisible = false
    counterView.updateMaxValue(allowedSpaces)
    builder.setView(counterView)

    // Buttons listeners
    builder.setPositiveButton(getString(positiveText).uppercase()) { _, _ ->
        val currentValue = counterView.getCurrentValue()
        positive(currentValue)
    }
    builder.setNegativeButton(getString(negativeText).uppercase()) { _, _ ->
        negative()
    }

    // Create and show dialog
    return builder.create().show()
}

fun Fragment.dialogWithSkuInfo(
    skuWithEntities: SkuWithEntities,
    @StringRes positiveText: Int,
    positive: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    builder.setCancelable(false)

    // Setup sku info details
    val viewInflater = LayoutInflater.from(this.context)
        .inflate(R.layout.dialog_sku_info_layout, this.requireView() as ViewGroup, false)

    val category = viewInflater.findViewById<AppCompatTextView>(R.id.category)
    val name = viewInflater.findViewById<AppCompatTextView>(R.id.name)
    val code = viewInflater.findViewById<AppCompatTextView>(R.id.code)
    val packet = viewInflater.findViewById<AppCompatTextView>(R.id.packet)

    val sku = skuWithEntities.sku
    category.text = sku.category
    name.text = sku.name
    code.text = sku.barcodeId
    packet.text = requireContext().getString(
        R.string.from_things,
        skuWithEntities.getCollected().formatTrim(),
        skuWithEntities.getTotal().formatTrim()
    )

    // Setup view
    builder.setView(viewInflater)

    // Buttons listeners
    builder.setPositiveButton(getString(positiveText).uppercase()) { _, _ -> positive() }

    // Create and show dialog
    return builder.create().show()
}

fun Fragment.dialogWithOrderInfo(
    orderWithEntities: OrderWithEntities,
    @StringRes positiveText: Int,
    positive: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    builder.setCancelable(false)

    // Setup sku info details
    val viewInflater = LayoutInflater.from(this.context)
        .inflate(R.layout.dialog_order_info_layout, this.requireView() as ViewGroup, false)

    val orderNumber = viewInflater.findViewById<AppCompatTextView>(R.id.orderNumber)
    val orderOpened = viewInflater.findViewById<AppCompatTextView>(R.id.orderOpened)
    val orderAddress = viewInflater.findViewById<AppCompatTextView>(R.id.orderAddress)
    val cargoSpaceNumber = viewInflater.findViewById<AppCompatTextView>(R.id.cargoSpaceNumber)
    val fullyPackedSku = viewInflater.findViewById<AppCompatTextView>(R.id.fullyPackedSku)
    val totalPackedProducts = viewInflater.findViewById<AppCompatTextView>(R.id.totalPackedProducts)

    val order = orderWithEntities.order
    val cargoSpaceCount = orderWithEntities.cargoSpaceList.sumOf { it.spaceIds.size }
    orderNumber.text = requireContext().getString(R.string.order_number_is, order.number)
    orderOpened.text = order.getFormatDate()
    orderAddress.text = order.address
    cargoSpaceNumber.text = cargoSpaceCount.toString()
    fullyPackedSku.text = requireContext().getString(
        R.string.from_things,
        orderWithEntities.getCollectedSkuCount().toString(),
        orderWithEntities.skuList.size.toString()
    )
    totalPackedProducts.text = requireContext().getString(
        R.string.from_things,
        orderWithEntities.getCollectedGoodsCount().toString(),
        orderWithEntities.getTotalGoods().toString()
    )

    // Setup view
    builder.setView(viewInflater)

    // Buttons listeners
    builder.setPositiveButton(getString(positiveText).uppercase()) { _, _ -> positive() }

    // Create and show dialog
    return builder.create().show()
}

fun Fragment.dialogWithImage(
    imageBase64: String,
    @StringRes positiveText: Int,
    positive: () -> Unit
) {
    val builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.AlertDialogTheme)
    builder.setCancelable(false)

    // Setup sku info details
    val viewInflater = LayoutInflater.from(this.context)
        .inflate(R.layout.dialog_image_layout, this.requireView() as ViewGroup, false)

    val image = viewInflater.findViewById<ImageView>(R.id.image)

    val decodedString = Base64.decode(imageBase64, Base64.DEFAULT)
    image.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size));

    // Setup view
    builder.setView(viewInflater)

    // Buttons listeners
    builder.setPositiveButton(getString(positiveText).uppercase()) { _, _ -> positive() }

    // Create and show dialog
    return builder.create().show()
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}