package com.qweex.smashuphelper;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class Summary extends AppCompatActivity{
    Selecting.Player[] players;
    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Parcelable[] pp = getIntent().<Selecting.Player>getParcelableArrayExtra("players");
        players = new Selecting.Player[pp.length];
        for(int i=0; i<pp.length; i++)
            players[i] = (Selecting.Player) pp[i];

        setContentView(listView = new ListView(this));
        listView.setAdapter(new SummaryAdapter(this, android.R.layout.simple_list_item_2, players));
    }

    class SummaryAdapter extends ArrayAdapter<Selecting.Player> {
        Selecting.Player[] players;

        public SummaryAdapter(Context context, int resource, Selecting.Player[] players) {
            super(context, resource, android.R.id.text1, players);
            this.players = players;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            convertView.setFocusable(false);
            convertView.setClickable(false);
            ((TextView)(convertView.findViewById(android.R.id.text2))).setText(
                    String.format("%s & %s", players[position].factions[0], players[position].factions[1])
            );
            ((TextView)(convertView.findViewById(android.R.id.text1))).setText(players[position].name);
            return convertView;
        }
    }
}
