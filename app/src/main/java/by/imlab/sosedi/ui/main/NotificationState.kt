package by.imlab.sosedi.ui.main

sealed class NotificationState {
    object Suspense : NotificationState()
    object NewOrders : NotificationState()
}