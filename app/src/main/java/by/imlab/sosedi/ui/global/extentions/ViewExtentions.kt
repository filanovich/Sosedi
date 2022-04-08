package by.imlab.sosedi.ui.global.extentions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView

fun RecyclerView.setDivider(@DrawableRes drawableRes: Int) {
    val divider = DividerItemDecoration(
        this.context,
        DividerItemDecoration.VERTICAL
    )
    val drawable = ContextCompat.getDrawable(
        this.context,
        drawableRes
    )
    drawable?.let {
        divider.setDrawable(it)
        addItemDecoration(divider)
    }
}

fun Context.checkNetworkState(): Boolean {
    val connectivityManager = this
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}

fun ImageView.startRotateAnimation() {
    val rotate = RotateAnimation(
        0.0f,
        360.0f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )
    rotate.duration = 2.toLong() * 500
    rotate.repeatCount = Animation.INFINITE
    this.startAnimation(rotate)
}

fun ImageView.endRotateAnimation() {
    this.clearAnimation()
}

fun NavigationView.setupWithNavController(
    navController: NavController,
    onMenuItemSelected: (MenuItem) -> Boolean
) {
    NavigationUI.setupWithNavController(
        this,
        navController
    ) // because this does more than set the nav view listener
    setNavigationItemSelectedListener { item ->
        if (onMenuItemSelected(item)) {
            return@setNavigationItemSelectedListener true
        }
        NavigationUI.onNavDestinationSelected(item, navController).also {
            val parent = parent
            if (parent is DrawerLayout) {
                parent.closeDrawer(this)
            } else {
                findBottomSheetBehavior()?.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }
}

private fun View.findBottomSheetBehavior(): BottomSheetBehavior<*>? {
    val params = layoutParams
    if (params !is CoordinatorLayout.LayoutParams) {
        return (parent as? View)?.findBottomSheetBehavior()
    }
    return params.behavior as? BottomSheetBehavior<*>
}