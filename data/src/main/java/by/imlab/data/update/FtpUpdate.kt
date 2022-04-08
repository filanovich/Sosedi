package by.imlab.data.update

import android.text.TextUtils
import by.imlab.data.model.Result
import by.imlab.data.network.model.Update
import by.imlab.data.preferences.Prefs
import com.google.gson.Gson
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class FtpUpdate {

    fun checkUpdate(): Result {

        val ftpFile = "by.imlab.sosedi.json";
        val localFile = Prefs.filesDir + "by.imlab.sosedi.json"

        val result = downloadFile(ftpFile, localFile)
        if (result is Result.Error)
            return result

        var gson = Gson()

        val json = File(localFile).readText();

        return try {
            val root = JSONObject(json)
            val apkData = root.getJSONObject("apkData")
            val versionCode = apkData.getLong("versionCode");
            val versionName = apkData.getString("versionName");
            val outputFile = apkData.getString("outputFile");

            val update = Update(versionCode = versionCode, versionName = versionName, outputFile = outputFile)
            Result.Success(update)

        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    fun downloadFile(ftpFile: String, localFile: String): Result {

        val ftp = FTPClient()
        val errorString: String

        ftp.controlEncoding = "UTF-8"
        ftp.connectTimeout = 5000
        try {
            ftp.connect(Prefs.updateUrl, 21)
        } catch (e: Exception) {
            return Result.Error(e.message)
        }

        ftp.enterLocalPassiveMode()

        if (!ftp.login(Prefs.updateUser, Prefs.updatePassword)) {
            errorString = ftp.replyString;
            return Result.Error(errorString);
        }

        if (!ftp.changeWorkingDirectory("android")) {
            errorString = ftp.replyString;
            return Result.Error(errorString);
        }

        if (!TextUtils.isEmpty(Prefs.updateDir)) {
            if (!ftp.changeWorkingDirectory(Prefs.updateDir)) {
                errorString = ftp.replyString;
                return Result.Error(errorString);
            }
        }

        val outputStream = BufferedOutputStream(FileOutputStream(File(localFile)))
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        if (!ftp.retrieveFile(ftpFile, outputStream)) {
            outputStream.close();
            errorString = ftp.replyString;
            return Result.Error(errorString);
        }
        outputStream.close();

        ftp.logout();
        ftp.disconnect();

        return Result.Success(null)
    }

    fun downloadUpdate(apkFile: String): Result {
        val ftpFile = apkFile;
        val localFile = Prefs.filesDir + apkFile
        val result = downloadFile(ftpFile, localFile)
        if (result is Result.Success<*>)
            return Result.Success(apkFile)
        return result
    }
}