package com.qweex.smashuphelper;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

public class Persistable implements Serializable, Parcelable {
    public String name;
    private boolean disabled;
    final static private String PREF_NAME = "persistent";
    private static SharedPreferences prefs;

    Persistable(String key) {
        name = key;
        disabled = prefs.getBoolean(String.format("%s[%s]", getClass().getName(), name), true);
        Log.d("Persistable", key + " " + disabled);
    }

    Persistable(Parcel src) {
        this(src.readString());
        setDisabled(src.readByte() != 0);
    }

    public void setDisabled(boolean d) {
        disabled = d;
        prefs.edit().putBoolean(
                String.format("%s[%s]", getClass().getName(), name),
                disabled
        ).apply();
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (isDisabled() ? 1 : 0));
    }


    public static Parcelable.Creator<Persistable> CREATOR = new Parcelable.Creator<Persistable>() {
        @Override
        public Persistable createFromParcel(Parcel in) {
            return new Persistable(in);
        }

        @Override
        public Persistable[] newArray(int size) {
            return new Persistable[size];
        }
    };

    public static void init(Context c) {
        prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
