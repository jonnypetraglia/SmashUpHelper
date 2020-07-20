package com.qweex.smashuphelper


import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.io.Serializable


@SuppressLint("ParcelCreator")
open class Persistable internal constructor(var name: String) : Serializable, Parcelable {
    private var disabled: Boolean = false

    private fun prefKey(): String {
        return String.format("%s[%s]", javaClass.name, name)
    }

    var isDisabled: Boolean
        get() = disabled
        set(d) {
            disabled = d
            Log.d("Persisting", prefKey() + " = " + d)
            prefs!!.edit().putBoolean(prefKey(), disabled).apply()
        }

    init {
        disabled = prefs!!.getBoolean(prefKey(), false)
        Log.d("Persistable", "$name $disabled")
    }

    internal constructor(src: Parcel) : this(src.readString()) {
        val x = src.readByte()
        Log.d("Persistparcle", "$name=$x")
        isDisabled = x.toInt() != 0
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        Log.d("PersistparcleW", "$name=$disabled")
        dest.writeString(name)
        dest.writeByte((if (disabled) 1 else 0).toByte())
    }

    companion object CREATOR : Parcelable.Creator<Persistable> {
        override fun createFromParcel(parcel: Parcel): Persistable {
            return Persistable(parcel)
        }

        override fun newArray(size: Int): Array<Persistable?> {
            return arrayOfNulls(size)
        }
        private var prefs: SharedPreferences? = null
        private const val PREF_NAME = "persistent"
        fun init(c: Context)
        {
            prefs = c.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        }
    }

}
