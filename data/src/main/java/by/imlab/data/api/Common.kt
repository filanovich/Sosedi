package by.imlab.data.api

import android.util.Base64
import by.imlab.data.preferences.Prefs

class Common() {

    fun getBASE_UR(): String {
        var url = Prefs.url  //globalPreferences.url
        if (!url.trim().startsWith("http://"))
            url = String.format("http://%s", url)

        return url;
    }

    val retrofitService: APIService
        get() = RetrofitClient.getClient(getBASE_UR()).create(APIService::class.java)

    fun auth(username: String, pass: String): String? {
        val credentials = "$username:$pass"
        return "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }
}