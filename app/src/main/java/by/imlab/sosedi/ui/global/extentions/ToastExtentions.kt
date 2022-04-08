package by.imlab.sosedi.ui.global.extentions

import android.app.Activity
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment


@IntDef(value = [Toast.LENGTH_SHORT, Toast.LENGTH_LONG])
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class Duration

fun Activity.toast(text: String, @Duration duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Activity.toast(@StringRes resId: Int, @Duration duration: Int = Toast.LENGTH_SHORT) {
    toast(getString(resId), duration)
}

fun Fragment.toast(text: String, @Duration duration: Int = Toast.LENGTH_SHORT) {
    requireActivity().toast(text, duration)
}

fun Fragment.toast(@StringRes resId: Int, @Duration duration: Int = Toast.LENGTH_SHORT) {
    toast(getString(resId), duration)
}