package com.qweex.smashuphelper;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;


/*
    - "Change Factions" button
    - # of people
    - # Mulligans
      - display a line for each random; so "(1st) Mulligans: 1", "2nd Mulligans: 0"
    - Type
      - 1 random 1 random
      - 1 random 1 random
      - 2 randoms
      - 2 picks
      - 2 out of 3
        - Mulligan reshuffles all 3?
      - Snake (randomly select enough for everyone; go round)
    - Secret? (don't show what other people have picked)
 */


public class MainActivity extends AppCompatActivity {
    ArrayList<Expansion> expansions = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            loadJSONFromAsset();
            setContentView(R.layout.activity_main);
            findViewById(R.id.go).setOnClickListener(startSelection);
            ((SeekBar)findViewById(R.id.playersSeek)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ((TextView)findViewById(R.id.players)).setText(Integer.toString(seekBar.getProgress()+2));
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    class Expansion {
        public String name;
        public String[] factions;
        public boolean disabled;

        public Expansion(String key, JSONArray jsonArray) throws JSONException {
            name = key;
            factions = new String[jsonArray.length()];
            for(int i=0; i<jsonArray.length(); i++)
                factions[i] = jsonArray.getString(i);
        }
        public Expansion(String key, JSONArray jsonArray, boolean disabled) throws JSONException {
            this(key, jsonArray);
            setDisabled(disabled);
        }

        public void setDisabled(boolean d) {
            disabled = d;
        }
    }

    View.OnClickListener startSelection = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View v) {
            ArrayList<String> factions = new ArrayList<>();
            Intent intent = new Intent(MainActivity.this, Selecting.class);
            for(Expansion exp : expansions)
                if(!exp.disabled)
                    for(String fac : exp.factions)
                        factions.add(fac);
            intent.putExtra("factions", factions);
            intent.putExtra("players", ((SeekBar)findViewById(R.id.playersSeek)).getProgress()+2);
            intent.putExtra("mulligans1", Integer.parseInt(((EditText)findViewById(R.id.mulligans1)).getText().toString()));
            intent.putExtra("mulligans2", Integer.parseInt(((EditText)findViewById(R.id.mulligans2)).getText().toString()));
            intent.putExtra("type_id", ((RadioGroup)findViewById(R.id.type_group)).getCheckedRadioButtonId());

            startActivity(intent);
        }
    };

    public void loadJSONFromAsset() throws IOException, JSONException {
        InputStream is = getAssets().open("expansions.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        JSONObject obj = new JSONObject(new String(buffer, "UTF-8"));
        Iterator<?> keys = obj.keys();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            Expansion exp = new Expansion(key, obj.getJSONArray(key));
            expansions.add(exp);
        }
    }
}
