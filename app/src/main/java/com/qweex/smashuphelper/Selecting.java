package com.qweex.smashuphelper;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/*

    - Random: "Press to draw"
      - If mulligan, display "Use Mulligan (# left)"
    - Pick: scroll through list
 */
public class Selecting extends AppCompatActivity {
    Player[] players;
    ListView pickListview;
    TextView otherPick;
    ArrayAdapter<String> factionAdapter;
    int selectType;
    int iter = -1;
    Random randomGenerator = new Random();
    int chosenFaction;

    int[] SelectIDs = new int[] {
            R.id.pick_random,
            R.id.random_pick,
            R.id.pick_pick,
            R.id.random_random
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectType = getIntent().getIntExtra("type_id", -1);
        players = new Player[ getIntent().getIntExtra("players", -1) ];
        for(int i=0; i<players.length; i++)
            players[i] = new Player( getIntent().getIntExtra("mulligans1", 0), getIntent().getIntExtra("mulligans2", 0) );

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

    void next() {
        ++iter;
        if(iter >= players.length * 2) {
            done();
            return;
        }
        setTitle("Player " + ((iter % players.length)+1) + " Faction " + ((iter / players.length)+1) );
        if(iter >= players.length) {
            switch(selectType) {
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
            switch(selectType) {
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
    void fadeIn(final View v) {
//        v.setAlpha(0.0f);
        v.animate()
                .alpha(0.0f)
                .x(getWindowManager().getDefaultDisplay().getWidth() / 2)
                .setDuration(0)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        v.animate()
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
        otherPick.setText(players[iter % players.length].factions[0]);
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
                ((TextView)findViewById(R.id.use_mulligan)).setText("Mulligan (" + mulliganCt + ")");
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
        otherPick.setText(players[iter % players.length].factions[0]);
        fadeIn(pickListview);
    }

    void done() {
        //TODO
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<players.length; i++)
            sb.append("Player ").append(i).append(": ").append(players[i].factions[0]).append(" & ").append(players[i].factions[1]).append("\n");
        new AlertDialog.Builder(this)
                .setMessage(sb.toString())
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    class Player {
        String[] factions = new String[2];
        int[] mulligans;
        public Player(int m1, int m2) {
            mulligans = new int[] {m1, m2};
        }
    }
}
