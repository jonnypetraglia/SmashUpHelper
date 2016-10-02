package com.qweex.smashuphelper;


import android.os.Parcel;
import android.os.Parcelable;

public class Faction extends Persistable {
    Faction(String key) {
        super(key);
    }

    Faction(Parcel src) {
        super(src);
    }

    public static Parcelable.Creator<Faction> CREATOR = new Parcelable.Creator<Faction>() {
        @Override
        public Faction createFromParcel(Parcel in) {
            return new Faction(in);
        }

        @Override
        public Faction[] newArray(int size) {
            return new Faction[size];
        }
    };
}
