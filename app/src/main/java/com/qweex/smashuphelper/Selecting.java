package com.qweex.smashuphelper;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


/* TODO:
    - UNDO
      - If
 */

public class Selecting extends AppCompatActivity {
    Player[] players;
    ListView pickListview;
    TextView otherPick;
    ArrayAdapter<String> factionAdapter;
    int selectMethod;
    int iter = -1;
    Random randomGenerator = new Random(System.currentTimeMillis());
    final static int FORCE_MULLIGAN = 1, FORCE_PICK = 2, UNDO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        selectMethod = extras.getInt("method_id", -1);
        players = new Player[ extras.getInt("players", -1) ];
        for(int i=0; i<players.length; i++)
            players[i] = new Player(
                    String.format("Player %d", i+1),
                    extras.getInt("mulligans1", 0),
                    extras.getInt("mulligans2", 0)
            );

        ArrayList<String> factions = getIntent().getStringArrayListExtra("factions");
        Collections.sort(factions);
        factionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, factions);
        pickListview = new ListView(this);
        pickListview.setAdapter(factionAdapter);
        pickListview.addHeaderView(otherPick = new TextView(this));
        pickListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectFaction((TextView) view);
            }
        });

        next();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, FORCE_MULLIGAN, Menu.NONE, "Legit need to random")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, FORCE_PICK, Menu.NONE, "Legit need to pick")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, UNDO, Menu.NONE, "Undo last select")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case FORCE_MULLIGAN:
                doRandom();
                break;
            case FORCE_PICK:
                doPick();
                break;
            case UNDO:
                if(iter==0)
                    break;
                --iter;
                String lastChoice = players[iter % players.length].factions[iter / players.length];
                Log.d("last", "Player " + (iter % players.length) + " faction " + (iter / players.length) + "!");
                factionAdapter.add(lastChoice);
                factionAdapter.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                --iter;
                next();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void next() {
        ++iter;
        if(iter >= players.length * 2) {
            done();
            return;
        }
        setTitle(
                String.format("%s - Faction %d", players[iter % players.length].name, ((iter / players.length)+1))
        );
        if(iter >= players.length) {
            switch(selectMethod) {
                case R.id.pick_random:
                case R.id.random_random:
                    doRandom();
                    break;
                case R.id.pick_pick:
                case R.id.random_pick:
                    doPick();
                    break;
            }
        } else {
            switch(selectMethod) {
                case R.id.random_pick:
                case R.id.random_random:
                    doRandom();
                    break;
                case R.id.pick_pick:
                case R.id.pick_random:
                    doPick();
                    break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void fadeIn(final View viewToFade) {
        viewToFade.animate()
                .alpha(0.0f)
                .x(getWindowManager().getDefaultDisplay().getWidth() / 2)
                .setDuration(0)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        viewToFade.animate()
                                .alpha(1.0f)
                                .x(0)
                                .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                        ;
                    }
                });
    }

    void doRandom() {
        setContentView(R.layout.random);
        findViewById(R.id.draw).setOnClickListener(drawRandom);

        TextView otherPick = (TextView) findViewById(R.id.other);
        otherPick.setVisibility(iter >= players.length ? View.VISIBLE : View.GONE);
        otherPick.setText(
                String.format("%s and:", players[iter % players.length].factions[0])
        );
        fadeIn(findViewById(R.id.random_layout));
    }

    View.OnClickListener drawRandom = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String faction = factionAdapter.getItem(randomGenerator.nextInt(factionAdapter.getCount()));
            ((TextView)findViewById(R.id.faction)).setText(faction);
            ((Button)findViewById(R.id.draw)).setText("Accept");
            int mulliganCt = players[iter % players.length].mulligans[iter / players.length];
            if(mulliganCt==0) {
                findViewById(R.id.use_mulligan).setVisibility(View.GONE);
            } else {
                findViewById(R.id.use_mulligan).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.use_mulligan)).setText(
                        String.format("Mulligan (%d)", mulliganCt)
                );
            }
            findViewById(R.id.use_mulligan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    players[iter % players.length].mulligans[iter / players.length]--;
                    drawRandom.onClick(v);
                }
            });
            findViewById(R.id.draw).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectFaction((TextView) findViewById(R.id.faction));
                }
            });
        }
    };

    void selectFaction(TextView textView) {
        String faction = textView.getText().toString();
        players[iter % players.length].factions[iter / players.length] = faction;
        factionAdapter.remove(faction);
        next();
    }


    void doPick() {
        setContentView(pickListview);

        otherPick.setVisibility(iter >= players.length ? View.VISIBLE : View.GONE);
        otherPick.setText(
                String.format("%s and:", players[iter % players.length].factions[0])
        );
        fadeIn(pickListview);
    }

    void done() {
        //TODO

        Intent intent = new Intent(this, Summary.class);
        intent.putExtra("players", players);
        startActivity(intent);
        finish();
    }

    public static class Player implements Parcelable {
        String name;
        String[] factions = new String[2];
        int[] mulligans;

        public Player(String n, int m1, int m2) {
            name = n;
            mulligans = new int[] {m1, m2};
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
}
