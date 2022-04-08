package by.imlab.data.repository

import by.imlab.data.api.Common
import by.imlab.data.api.APIService
import by.imlab.data.api.model.Login
import by.imlab.data.api.model.LoginResponse
import by.imlab.data.datasource.LoginDataSource
import by.imlab.data.network.model.User
import by.imlab.data.model.Result
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(
    private val dataSource: LoginDataSource,
    private val common: Common
) {

//    private val onLoginListener: OnLoginListener? = null

//    interface OnLoginListener {
//        fun onComplete(result: Result<User>)
//    }

//    private val listener: LoginListener? = null

    // in-memory cache of the loggedInUser object
    var user: User? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun authentication(username: String, password: String, callback: (result: Result?) -> Unit) {

        val service: APIService = common.retrofitService

        service.authentication(
            "Picker.authentication",
            Login(username, password)
        ).enqueue(object : Callback<LoginResponse> {

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                callback(Result.Error(t.message))
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (!response.isSuccessful) {
                    callback(Result.Error(response.message()))
                } else {
                    val resp: LoginResponse  = response.body() as LoginResponse
                    if (resp.result) {
                        val user = User(resp.token, resp.displayName)
                        setLoggedInUser(user)
                        callback.invoke(Result.Success(user))
                    } else {
                        callback(Result.Error(resp.description))
                    }
                }
            }
        })
    }

    fun logout(callback: (result: Result?) -> Unit) {

        if (!isLoggedIn) {
            callback.invoke(Result.Success(null))
            return;
        }

        val service: APIService = common.retrofitService

        var token: String? = ""
        if (isLoggedIn)
            token = user?.token

        service.logout("Picker.logout", token)
            .enqueue(object : Callback<Void> {

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    callback(Result.Error(t.message))
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        callback(Result.Error(response.message()))
                    } else {
                        user = null
                        callback.invoke(Result.Success(null))
                    }
                }
            })
    }

    private fun setLoggedInUser(loggedInUser: User) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}