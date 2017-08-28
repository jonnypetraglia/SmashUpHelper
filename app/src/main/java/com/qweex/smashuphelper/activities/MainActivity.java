package com.qweex.smashuphelper.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qweex.smashuphelper.R;
import com.qweex.smashuphelper.objects.Expansion;
import com.qweex.smashuphelper.objects.Faction;
import com.qweex.smashuphelper.objects.Persistable;

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
    SeekBar playersSeek;
    EditText mulligans1, mulligans2;
    RadioGroup methodGroup;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putAll(getCurrentConfig());
        super.onSaveInstanceState(outState);
    }

    protected Bundle getCurrentConfig() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("expansions", expansions);
        bundle.putInt("players", playersSeek.getProgress()+2);
        bundle.putInt("mulligans1", Integer.parseInt(mulligans1.getText().toString()));
        bundle.putInt("mulligans2", Integer.parseInt(mulligans2.getText().toString()));
        bundle.putInt("method_id", methodGroup.getCheckedRadioButtonId());
        return bundle;
    }

    protected void restoreConfig(Bundle b) {
        expansions = (ArrayList<Expansion>) b.getSerializable("expansions");
        mulligans1.setText(String.valueOf(b.getInt("mulligans1")));
        mulligans2.setText(String.valueOf(b.getInt("mulligans2")));
        ((RadioButton)findViewById(b.getInt("method_id"))).setChecked(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Persistable.init(getApplicationContext());

        if(savedInstanceState!=null)
            restoreConfig(savedInstanceState);
        else {
            try {
                loadJSONFromAsset();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }

        setContentView(R.layout.activity_main);

        playersSeek = (SeekBar)findViewById(R.id.playersSeek);
        mulligans1 = (EditText)findViewById(R.id.mulligans1);
        mulligans2 = (EditText)findViewById(R.id.mulligans2);
        methodGroup = ((RadioGroup)findViewById(R.id.method_group));


        findViewById(R.id.factions).setOnClickListener(setFactions);
        findViewById(R.id.go).setOnClickListener(startSelection);

        methodGroup.setOnCheckedChangeListener(this);
        this.onCheckedChanged(methodGroup, methodGroup.getCheckedRadioButtonId());

        ((SeekBar) findViewById(R.id.playersSeek)).setOnSeekBarChangeListener(this);
    }

    View.OnClickListener startSelection = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View v) {
            ArrayList<String> factions = new ArrayList<>();
            for(Expansion exp : expansions)
                if(!exp.isDisabled())
                    for(Faction fac : exp.factions)
                        if(!fac.isDisabled())
                            factions.add(fac.name);

            Bundle bundle = getCurrentConfig();

            if(factions.size() < (bundle.getInt("players")*2)) {
                Toast.makeText(MainActivity.this, "You don't have enough factions selected for this many players! Select more in the 'Change Factions' option.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, Selecting.class);
            intent.putExtras(bundle);
            intent.putExtra("factions", factions);
            startActivity(intent);
        }
    };

    View.OnClickListener setFactions = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ChangeFactions.class);
            intent.putExtra("expansions", expansions);
            startActivityForResult(intent, 100);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100) {
            expansions = data.getParcelableArrayListExtra("expansions");
        }
    }

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
        mulligans1.setVisibility(
                (checkedId == R.id.random_pick || checkedId == R.id.random_random)
                ? View.VISIBLE : View.INVISIBLE
        );
        mulligans2.setVisibility(
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
