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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;


/*
TODO:
  - Settings
    - Taboos? (e.g. no zombots)
      - "randomly" or "ever"
    - more methods
      - 2 out of 3
        - Mulligan reshuffles all 3?
      - Snake (randomly select enough for everyone; go round)
 */


public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    ArrayList<Expansion> expansions = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Persistable.init(getApplicationContext());

        try {
            loadJSONFromAsset();
            setContentView(R.layout.activity_main);

            findViewById(R.id.factions).setOnClickListener(setFactions);
            findViewById(R.id.go).setOnClickListener(startSelection);

            RadioGroup methods = ((RadioGroup)findViewById(R.id.method_group));
            methods.setOnCheckedChangeListener(this);
            this.onCheckedChanged(methods, methods.getCheckedRadioButtonId());

            ((SeekBar)findViewById(R.id.playersSeek)).setOnSeekBarChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    View.OnClickListener startSelection = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View v) {
            ArrayList<String> factions = new ArrayList<>();
            Intent intent = new Intent(MainActivity.this, Selecting.class);
            for(Expansion exp : expansions)
                if(!exp.isDisabled())
                    for(Faction fac : exp.factions)
                        if(!fac.isDisabled())
                            factions.add(fac.name);
            intent.putExtra("factions", factions);
            intent.putExtra("players", ((SeekBar)findViewById(R.id.playersSeek)).getProgress()+2);
            intent.putExtra("mulligans1", Integer.parseInt(((EditText)findViewById(R.id.mulligans1)).getText().toString()));
            intent.putExtra("mulligans2", Integer.parseInt(((EditText)findViewById(R.id.mulligans2)).getText().toString()));
            intent.putExtra("method_id", ((RadioGroup)findViewById(R.id.method_group)).getCheckedRadioButtonId());

            startActivity(intent);
        }
    };

    View.OnClickListener setFactions = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ChangeFactions.class);
            intent.putExtra("expansions", expansions);
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

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        findViewById(R.id.mulligans1).setVisibility(
                (checkedId == R.id.random_pick || checkedId == R.id.random_random)
                ? View.VISIBLE : View.INVISIBLE
        );
        findViewById(R.id.mulligans2).setVisibility(
                (checkedId == R.id.pick_random || checkedId == R.id.random_random)
                ? View.VISIBLE : View.INVISIBLE
        );
        findViewById(R.id.mulligansText).setVisibility(
                (checkedId == R.id.random_pick || checkedId == R.id.pick_random || checkedId == R.id.random_random)
                ? View.VISIBLE : View.INVISIBLE
        );
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ((TextView)findViewById(R.id.players)).setText(Integer.toString(seekBar.getProgress()+2));
    }

    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
}
