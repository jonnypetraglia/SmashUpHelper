package com.qweex.smashuphelper

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.text.Charsets.UTF_8

class ExpansionsLoader() {

    var changeFactions : ChangeFactions? = null
    var parentActivity : Activity? = null

    internal var sharedPreferences: SharedPreferences? = null


    constructor(activity : Activity) : this() {
        this.parentActivity = activity
        sharedPreferences = parentActivity!!.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
    }

    constructor(changeFactions : ChangeFactions) : this() {
        this.parentActivity = changeFactions
        this.changeFactions = changeFactions
        sharedPreferences = parentActivity!!.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
    }

    fun saveExpansions(expansions: ArrayList<Expansion>) {
        Log.d("!!!", "saveExtensionsToCache " + expansions)
        val editor = sharedPreferences!!.edit()
        val jsonElements = ArrayList<String>()
        Log.d("!!!", expansions.size.toString() + "==" + jsonElements.size + " " )
        for(exp in expansions) {
            jsonElements.add("\"" + exp.name + "\": " + exp.factionsAsJSON().toString())
        }
        editor.putString(EXTENSIONS_KEY, "{ " + TextUtils.join(",\n", jsonElements) + " }")
        editor.apply()
    }

    fun saveServiceToCache(service: String, username: String, id: String) {
        val editor = sharedPreferences!!.edit()
        editor.putString(SERVICE_KEY, service)
        editor.putString(USERNAME_KEY, username)
        editor.putString(ID_KEY, id)
        editor.apply()
    }

    fun saveUrlToCache(url: String) {
        val editor = sharedPreferences!!.edit()
        editor.putString(URL_KEY, url)
        editor.apply()
    }

    @Throws(JSONException::class, IOException::class)
    fun loadFromCache(): ArrayList<Expansion> {
        Log.d("!!!", "loadFrom Cache")
        val sharedPreferences = parentActivity!!.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val fileContents = sharedPreferences.getString(EXTENSIONS_KEY, null)
        try {
            return loadJSON(JSONObject(fileContents))
        } catch (error: Exception) {
            Log.e("!!!", error.toString())
            return loadFromAsset()
        }
    }

    @Throws(IOException::class, JSONException::class)
    fun loadFromAsset(): ArrayList<Expansion> {
        Log.d("!!!", "loadFrom Asset")
        val `is` = parentActivity!!.assets.open("expansions.json")
        val size = `is`.available()
        val buffer = ByteArray(size)
        `is`.read(buffer)
        `is`.close()
        return loadJSON(JSONObject(String(buffer, UTF_8)))
    }

    @Throws(JSONException::class, IOException::class)
    fun loadFromURL(fullURL: String): ArrayList<Expansion> {
        Log.d("!!!", "loadFrom URL")
        val derp = OmgAndroidNeedsEasierHttpRequestCapability()
        derp.setExpansionsLoader(this)
        derp.execute(fullURL)
        return ArrayList<Expansion>();
    }

    fun fetchedError(error : Exception) {
        changeFactions!!.importFailure(error)
    }

    fun fetchedJSON(json : String) {
        Log.d("!!!", "fetchedJSON: " + json)
        val expansions = loadJSON(JSONObject(json))
        saveExpansions(expansions)
        changeFactions!!.importSuccess(expansions)
    }

    @Throws(IOException::class, JSONException::class)
    fun loadFromGithubGist(username: String, gistID: String): ArrayList<Expansion> {
        Log.d("!!!", "loadFrom Github")
        return loadFromURL(String.format("https://gist.githubusercontent.com/%s/%s/raw/expansions.json", username, gistID))
    }

    @Throws(IOException::class, JSONException::class)
    fun loadFromBitbucketSnippet(username: String, snippetID: String): ArrayList<Expansion> {
        Log.d("!!!", "loadFrom Bitbucket")
        return loadFromURL(String.format("https://bitbucket.org/!api/2.0/snippets/%s/%s/master/files/expansions.json", username, snippetID))
    }

    @Throws(IOException::class, JSONException::class)
    fun loadFromGitlabSnippet(snippetID: String): ArrayList<Expansion> {
        Log.d("!!!", "loadFrom Github")
        return loadFromURL(String.format("https://gitlab.com/snippets/%s/raw", snippetID))
    }

    @Throws(JSONException::class)
    private fun loadJSON(obj: JSONObject): ArrayList<Expansion> {
        val expansions = ArrayList<Expansion>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next() as String
            val exp = Expansion(key, obj.getJSONArray(key))
            expansions.add(exp)
        }
        return expansions
    }

    companion object {
        internal val SHARED_PREFS = "sharedPrefs"

        internal val EXTENSIONS_KEY = "expansions"
        internal val SERVICE_KEY = "service"
        internal val USERNAME_KEY = "username"
        internal val ID_KEY = "id"
        internal val URL_KEY = "url"
    }
}
