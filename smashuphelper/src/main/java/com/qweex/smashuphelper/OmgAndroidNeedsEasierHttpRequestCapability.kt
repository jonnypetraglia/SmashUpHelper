package com.qweex.smashuphelper

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class OmgAndroidNeedsEasierHttpRequestCapability : AsyncTask<String?, Void?, String?>() {
    lateinit var hodor : ExpansionsLoader
    var xmlResult : String = ""
    var error : Exception? = null

    fun setExpansionsLoader(el : ExpansionsLoader) {
        hodor = el
    }
    override fun doInBackground(vararg p0: String?): String? {
        val url = URL(p0[0])
        Log.d("!", url.toString())
        val result = StringBuffer()
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val reader = BufferedReader(InputStreamReader(BufferedInputStream(urlConnection.inputStream)))
            var line: String?
            line = reader.readLine()
            while(line != null) {
                Log.d("!!!!", "" + line)
                result.append(line)
                line = reader.readLine()
            }
            xmlResult = result.toString();
        } catch(error : Exception) {
            this.error = error
            return null
        } finally {
            urlConnection.disconnect()
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        if(error != null) {
            hodor.fetchedError(error!!)
        } else {
            hodor.fetchedJSON(xmlResult)
        }
        super.onPostExecute(result)
    }
}