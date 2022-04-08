package by.imlab.sosedi.ui.settings

import android.content.Context
import android.preference.PreferenceManager
import android.widget.Toast
import by.imlab.data.preferences.Prefs
import by.imlab.sosedi.R

class Preferences {

    fun readPreferences(context: Context) {

        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);

        val defaultUrl = "http://i.sosedi.by:7695"
        var str = sharedPreference.getString(context.getString(R.string.preferences_server), defaultUrl).toString();
        if (!str.trim().startsWith("http://"))
            str = String.format("http://%s", str)

        Prefs.url = str

        Prefs.updateUrl = sharedPreference.getString(context.getString(R.string.preferences_updateapp_server), "").toString();
        Prefs.updateUser = sharedPreference.getString(context.getString(R.string.preferences_updateapp_user), "").toString();
        Prefs.updatePassword = sharedPreference.getString(context.getString(R.string.preferences_updateapp_password), "").toString();
        Prefs.updateDir = sharedPreference.getString(context.getString(R.string.preferences_updateapp_dir), "").toString();

        Prefs.filesDir = context.getFilesDir().absolutePath + "/"

        //Toast.makeText(context, Prefs.updateUrl + " " + Prefs.updateUser + " " + Prefs.updatePassword + " " + Prefs.updateDir, Toast.LENGTH_LONG).show()
    }

/*    fun savePreferences(context: Context) {
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        val editor = sharedPreference.edit()
        editor.putString(context.getString(R.string.preferences_server), globalPrefrences.url)
        editor.putString(context.getString(R.string.preferences_updateapp_server), globalPrefrences.updateUrl)
        editor.putString(context.getString(R.string.preferences_updateapp_user), globalPrefrences.updateUser)
        editor.putString(context.getString(R.string.preferences_updateapp_password), globalPrefrences.updatePassword)
        editor.putString(context.getString(R.string.preferences_updateapp_dir), globalPrefrences.updateDir)
        editor.commit()
    }*/
}