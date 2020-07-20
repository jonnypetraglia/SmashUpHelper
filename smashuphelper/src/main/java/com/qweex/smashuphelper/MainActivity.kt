package com.qweex.smashuphelper

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import java.util.*


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


class MainActivity : AppCompatActivity(), OnCloseNotice, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    internal var expansions = ArrayList<Expansion>()

    internal var startSelection: View.OnClickListener = View.OnClickListener {
        val factions = ArrayList<String>()
        val intent = Intent(this@MainActivity, Selecting::class.java)
        for (exp in expansions)
            if (!exp.isDisabled)
                for (fac in exp.factions)
                    if (!fac!!.isDisabled)
                        factions.add(fac.name)
        intent.putExtra("factions", factions)
        intent.putExtra("players", (findViewById(R.id.playersSeek) as SeekBar).progress + 2)
        intent.putExtra("mulligans1", Integer.parseInt((findViewById(R.id.mulligans1) as EditText).text.toString()))
        intent.putExtra("mulligans2", Integer.parseInt((findViewById(R.id.mulligans2) as EditText).text.toString()))
        intent.putExtra("method_id", (findViewById(R.id.method_group) as RadioGroup).checkedRadioButtonId)
        startActivity(intent)
    }

    internal var setFactions: View.OnClickListener = View.OnClickListener {
        val intent = Intent(this@MainActivity, ChangeFactions::class.java)
        intent.putExtra("expansions", expansions)
        startActivityForResult(intent, 100)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Persistable.init(applicationContext)
        try {
            expansions = ExpansionsLoader(this).loadFromCache()
            Log.d("!!!", expansions.toString() + "!")
            setContentView(R.layout.activity_main)

            findViewById(R.id.factions).setOnClickListener(setFactions)
            findViewById(R.id.go).setOnClickListener(startSelection)

            val methods = findViewById(R.id.method_group) as RadioGroup
            methods.setOnCheckedChangeListener(this)
            this.onCheckedChanged(methods, methods.checkedRadioButtonId)

            (findViewById(R.id.playersSeek) as SeekBar).setOnSeekBarChangeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
            SimpleNotice(this).showError(e, this)
        }
    }

    override fun OnCloseNotice(returnVal: Object) {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && data != null) {
            val derp : ArrayList<Expansion>? = data.getParcelableArrayListExtra("expansions")
            if(derp != null) {
                expansions = data.getParcelableArrayListExtra("expansions")
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        findViewById(R.id.mulligans1).visibility = if (checkedId == R.id.random_pick || checkedId == R.id.random_random)
            View.VISIBLE
        else
            View.INVISIBLE
        findViewById(R.id.mulligans2).visibility = if (checkedId == R.id.pick_random || checkedId == R.id.random_random)
            View.VISIBLE
        else
            View.INVISIBLE
        findViewById(R.id.mulligansText).visibility = if (checkedId == R.id.random_pick || checkedId == R.id.pick_random || checkedId == R.id.random_random)
            View.VISIBLE
        else
            View.INVISIBLE
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        (findViewById(R.id.players) as TextView).text = Integer.toString(seekBar.progress + 2)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}
