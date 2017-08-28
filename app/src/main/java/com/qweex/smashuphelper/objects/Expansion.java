package com.qweex.smashuphelper.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;


public class Expansion extends Persistable {

    public Faction[] factions;


    public Expansion(String key, JSONArray jsonArray) throws JSONException {
        super(key);
        factions = new Faction[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++)
            factions[i] = new Faction(jsonArray.getString(i));
        Log.d("New Expansion", key + " = " + isDisabled());
    }

    public Expansion(String key, JSONArray jsonArray, boolean disabled) throws JSONException {
        this(key, jsonArray);
        this.setDisabled(disabled);
    }

    public Expansion(Parcel src) {
        super(src);
        factions = src.createTypedArray(Faction.CREATOR);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedArray(factions, 0);
    }

    public static Parcelable.Creator<Expansion> CREATOR = new Parcelable.Creator<Expansion>() {
        @Override
        public Expansion createFromParcel(Parcel in) {
            return new Expansion(in);
        }

        @Override
        public Expansion[] newArray(int size) {
            return new Expansion[size];
        }
    };
}
