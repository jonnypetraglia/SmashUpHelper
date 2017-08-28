package com.qweex.smashuphelper.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Player implements Parcelable {
    public String name;
    public String[] factions = new String[2];
    public int[] mulligans;
    public List<String> pastMulligans;

    public Player(String n, int m1, int m2) {
        name = n;
        mulligans = new int[]{m1, m2};
        pastMulligans = new ArrayList<String>();
    }

    protected Player(Parcel in) {
        name = in.readString();
        factions = in.createStringArray();
        mulligans = in.createIntArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStringArray(factions);
        dest.writeIntArray(mulligans);
    }

    public static Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}
