package by.imlab.sosedi.ui.global.helpers

import androidx.lifecycle.MutableLiveData
import by.imlab.core.extensions.set

class NavigationHelper {

    private val _lastClickedItem = MutableLiveData<Int>()
    val lastClickedItem = _lastClickedItem

    fun updateLastClickedItem(itemId: Int) {
        lastClickedItem.set(itemId)
    }
}