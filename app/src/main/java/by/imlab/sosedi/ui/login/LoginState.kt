package by.imlab.sosedi.ui.login

import by.imlab.data.network.model.Update

sealed class LoginState {
    object Loading : LoginState()
    object Suspense : LoginState()
    object DataValid : LoginState()
    data class LoginFailed(val message: String?) : LoginState()
    object LoginSuccess : LoginState()
    data class LogoutFailed(val message: String?) : LoginState()
    object LogoutSuccess : LoginState()
    object UsernameError : LoginState()
    object PasswordError : LoginState()

    data class CheckUpdateSuccess(val update: Update) : LoginState()
    data class CheckUpdateError(val message: String?) : LoginState()

    data class InstallUpdate(val apkFile: String) : LoginState()

    object SpotsDialogOn : LoginState()
//    object SpotsDialogOff: LoginState()
}