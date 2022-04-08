package by.imlab.data.datasource

import by.imlab.data.model.Result
import by.imlab.data.network.model.User


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

//    fun login(username: String, password: String): Result<User> {

//        val fakeUser = User(java.util.UUID.randomUUID().toString(), "Jane Doe")
//        return Result.Success(fakeUser)

//        return try {
//            // TODO: handle loggedInUser authentication
//            val service: RetrofitService = Common.retrofitService

//            service.authentication(
//                Common.auth(username, password),
//                "DataTerminal.authentication",
//                Authentication(username, "/wYg7J+zWq0hsPOwuLNEFwuEyIHx2apbZ1LJcDdrWTE="
//                )
//            ).enqueue(object : Callback<AuthenticationResponse> {
//                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
//                    Result.Error(IOException(t.message, t))
//                }

//                override fun onResponse(
//                    call: Call<AuthenticationResponse>,
//                    response: Response<AuthenticationResponse>
//                ) {
                    //var resp: AuthenticationResponse? = response.body()
                    //resp = null
//                    adapter = MyMovieAdapter(baseContext, response.body() as MutableList<Movie>)
//                    adapter.notifyDataSetChanged()
//                    recyclerMovieList.adapter = adapter
//                }
//            })


//            val responseCall: Call<AuthenticationResponse> = service.authentication(
//                Common.auth(username, password),
//                "DataTerminal.authentication",
//                Authentication(username, "/wYg7J+zWq0hsPOwuLNEFwuEyIHx2apbZ1LJcDdrWTE="
//                )
//            )

//            val response = responseCall.execute()
//            val error: String?

//            if (response.isSuccessful) {
//                val authenticationResponse = response.body()
//                if (authenticationResponse != null)
//                    error = authenticationResponse.error
//            } else {
//                error = if (Objects.isNull(response.errorBody())) response.message() else response.errorBody()!!.string()
//                if (error.contains("<title>")) error =
//                    mError.substring(mError.indexOf("<title>") + 7, mError.indexOf("</title>"))
//            }

//            val fakeUser = User(java.util.UUID.randomUUID().toString(), "Jane Doe")
//            Result.Success(fakeUser)

//        } catch (e: Throwable) {
//            Result.Error(IOException("Error logging in", e))
//        }
//    }

//    fun logout() {
//        // TODO: revoke authentication
//    }
}