package by.imlab.sosedi.ui.login

import android.app.AlertDialog
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import by.imlab.data.model.Result
import by.imlab.data.network.model.Update
import by.imlab.data.repository.LoginRepository
import by.imlab.data.update.FtpUpdate
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val ftpUpdate: FtpUpdate
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>().default(initialValue = LoginState.Suspense)
    val loginState: LiveData<LoginState> = _loginState

    fun login(context: Context, username: String, password: String) {
        _loginState.set(LoginState.Loading)

        var dialog: AlertDialog? = null
        if (context != null)
            dialog = SpotsDialog.Builder().setCancelable(false).setContext(context).build()

        dialog?.show();

        loginRepository.logout() {
            loginRepository.authentication(username, password) {
                dialog?.dismiss()
                when (it) {
                    is Result.Success<*> -> _loginState.set(LoginState.LoginSuccess)
                    is Result.Error -> _loginState.set(LoginState.LoginFailed(it.message))
                }
            }
        }
    }

    fun logout(context: Context) {

        var dialog: AlertDialog? = null
        if (context != null)
            dialog = SpotsDialog.Builder().setCancelable(false).setContext(context).build()

        dialog?.show();

        loginRepository.logout() { result ->

            dialog?.dismiss()

            when (result) {
                is Result.Success<*> -> _loginState.set(LoginState.LogoutSuccess)
                is Result.Error -> _loginState.set(LoginState.LogoutFailed(result.message))
            }
        }
    }

    fun checkUpdate() {

        _loginState.setValue(LoginState.SpotsDialogOn)

        viewModelScope.launch(Dispatchers.IO) {
            val result = ftpUpdate.checkUpdate();
            when (result) {
                is Result.Success<*> -> _loginState.set(LoginState.CheckUpdateSuccess(update = result.data as Update))
                is Result.Error -> _loginState.set(LoginState.LoginFailed(result.message))
            }
        }
    }

    fun downloadAndInstallUpdate(apkFile: String) {

        _loginState.setValue(LoginState.SpotsDialogOn)

        viewModelScope.launch(Dispatchers.IO) {
            val result: Result = ftpUpdate.downloadUpdate(apkFile)
            when (result) {
                is Result.Success<*> -> _loginState.postValue(LoginState.InstallUpdate(apkFile))
                is Result.Error -> _loginState.postValue(LoginState.LoginFailed(result.message))
            }
        }
    }

    fun validateCredentials(username: String, password: String) {
        val usernameValid = isUserNameValid(username)
        val passwordValid = isPasswordValid(password)
        if (!usernameValid) {
            _loginState.set(LoginState.UsernameError)
        } else if (!passwordValid) {
            _loginState.set(LoginState.PasswordError)
        } else {
            _loginState.set(LoginState.DataValid)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }
}