package com.qweex.smashuphelper


import android.os.Parcel
import android.os.Parcelable

class Faction : Persistable {
    internal constructor(key: String) : super(key) {}

    internal constructor(src: Parcel) : super(src) {}

    companion object CREATOR : Parcelable.Creator<Faction> {
        override fun createFromParcel(parcel: Parcel): Faction {
            return Faction(parcel)
        }

        override fun newArray(size: Int): Array<Faction?> {
            return arrayOfNulls(size)
        }
    }
}
