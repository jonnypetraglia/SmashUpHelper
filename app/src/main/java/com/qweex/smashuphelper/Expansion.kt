package com.qweex.smashuphelper

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.json.JSONArray
import org.json.JSONException


class Expansion : Persistable {

    var factions: Array<Faction?>


    @Throws(JSONException::class)
    constructor(key: String, jsonArray: JSONArray) : super(key) {
        factions = arrayOfNulls<Faction>(size = jsonArray.length())
        for (i in 0 until jsonArray.length())
            factions[i] = Faction(jsonArray.getString(i))
        Log.d("New Expansion", "$key = $isDisabled")
    }

    @Throws(JSONException::class)
    constructor(key: String, jsonArray: JSONArray, disabled: Boolean) : this(key, jsonArray) {
        this.isDisabled = disabled
    }

    constructor(src: Parcel) : super(src) {
        factions = src.createTypedArray(Faction.CREATOR)
    }

    fun factionsAsJSON(): JSONArray {
        val result = JSONArray()
        for (faction in factions) {
            result.put(faction!!.name)
        }
        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeTypedArray(factions, 0)
    }

    companion object CREATOR : Parcelable.Creator<Expansion> {
        override fun createFromParcel(parcel: Parcel): Expansion {
            return Expansion(parcel)
        }

        override fun newArray(size: Int): Array<Expansion?> {
            return arrayOfNulls(size)
        }
    }
}
