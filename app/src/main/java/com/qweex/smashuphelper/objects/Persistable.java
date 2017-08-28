package com.qweex.smashuphelper.objects;


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

    private String prefKey() {
        return String.format("%s[%s]", getClass().getName(), name);
    }

    Persistable(String key) {
        name = key;
        disabled = prefs.getBoolean(prefKey(), false);
        Log.d("Persistable", key + " " + disabled);
    }

    Persistable(Parcel src) {
        this(src.readString());
        byte x = src.readByte();
        Log.d("Persistparcle", name + "=" + x);
        setDisabled(x != 0);
    }

    public void setDisabled(boolean d) {
        disabled = d;
        Log.d("Persisting", prefKey() + " = " + d);
        prefs.edit().putBoolean(prefKey(), disabled).apply();
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
        Log.d("PersistparcleW", name + "=" + disabled);
        dest.writeString(name);
        dest.writeByte((byte) (disabled ? 1 : 0));
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
