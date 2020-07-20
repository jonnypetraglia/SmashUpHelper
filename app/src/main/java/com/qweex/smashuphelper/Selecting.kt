package com.qweex.smashuphelper

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import java.util.*


/* TODO:
    - UNDO
      - If
 */

class Selecting : AppCompatActivity() {
    internal var players : Array<Player?>? = null
    internal var pickListview: ListView?  = null
    internal var otherPick: TextView?  = null
    internal var factionAdapter: ArrayAdapter<String>?  = null
    internal var selectMethod: Int = 0
    internal var iter = -1
    internal var randomGenerator = Random(System.currentTimeMillis())
    internal var what = this

    internal var drawRandom : View.OnClickListener = View.OnClickListener {
        val faction = factionAdapter!!.getItem(randomGenerator.nextInt(factionAdapter!!.count))
        (findViewById(R.id.faction) as TextView).text = faction
        (findViewById(R.id.draw) as Button).text = "Accept"
        val playa : Array<Player?> = players!!
        val mulliganCt = playa[iter % playa.size]!!.mulligans[iter / playa.size]
        if (mulliganCt == 0) {
            findViewById(R.id.use_mulligan).visibility = View.GONE
        } else {
            findViewById(R.id.use_mulligan).visibility = View.VISIBLE
            (findViewById(R.id.use_mulligan) as TextView).text = String.format("Mulligan (%d)", mulliganCt)
        }
        findViewById(R.id.use_mulligan).setOnClickListener { v ->
            playa[iter % playa.size]!!.mulligans[iter / playa.size]--
            what.drawRandom.onClick(v);
        }
        findViewById(R.id.draw).setOnClickListener { selectFaction(findViewById(R.id.faction) as TextView) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState as Bundle?)
        val extras = intent.extras
        selectMethod = extras!!.getInt("method_id", -1)
        players = arrayOfNulls(extras.getInt("players", -1))
        for (i in players!!.indices)
            players!![i] = Player(
                    String.format("Player %d", i + 1),
                    extras.getInt("mulligans1", 0),
                    extras.getInt("mulligans2", 0)
            )

        val factions = intent.getStringArrayListExtra("factions")
        Collections.sort(factions)
        factionAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, factions)
        pickListview = ListView(this)
        pickListview!!.adapter = factionAdapter
        otherPick = TextView(this)
        pickListview!!.addHeaderView(otherPick)
        pickListview!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> selectFaction(view as TextView) }

        next()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, FORCE_MULLIGAN, Menu.NONE, "Legit need to random")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(Menu.NONE, FORCE_PICK, Menu.NONE, "Legit need to pick")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(Menu.NONE, UNDO, Menu.NONE, "Undo last select")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            FORCE_MULLIGAN -> doRandom()
            FORCE_PICK -> doPick()
            UNDO -> {
                if (iter != 0) {
                    --iter
                    val lastChoice = players!![iter % players!!.size]!!.factions[iter / players!!.size]
                    Log.d("last", "Player " + iter % players!!.size + " faction " + iter / players!!.size + "!")
                    factionAdapter!!.add(lastChoice)
                    factionAdapter!!.sort { o1, o2 -> o1.compareTo(o2) }
                    --iter
                    next()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    internal operator fun next() {
        ++iter
        if (iter >= players!!.size * 2) {
            done()
            return
        }
        title = String.format("%s - Faction %d", players!![iter % players!!.size]!!.name, iter / players!!.size + 1)
        if (iter >= players!!.size) {
            when (selectMethod) {
                R.id.pick_random, R.id.random_random -> doRandom()
                R.id.pick_pick, R.id.random_pick -> doPick()
            }
        } else {
            when (selectMethod) {
                R.id.random_pick, R.id.random_random -> doRandom()
                R.id.pick_pick, R.id.pick_random -> doPick()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun fadeIn(viewToFade: View) {
        viewToFade.animate()
                .alpha(0.0f)
                .x((windowManager.defaultDisplay.width / 2).toFloat())
                .setDuration(0)
                .withEndAction {
                    viewToFade.animate()
                            .alpha(1.0f)
                            .x(0f).duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                }
    }

    internal fun doRandom() {
        setContentView(R.layout.random)
        findViewById(R.id.draw).setOnClickListener(drawRandom)

        val otherPick = findViewById(R.id.other) as TextView
        otherPick.visibility = if (iter >= players!!.size) View.VISIBLE else View.GONE
        otherPick.text = String.format("%s and:", players!![iter % players!!.size]!!.factions[0])
        fadeIn(findViewById(R.id.random_layout))
    }

    internal fun selectFaction(textView: TextView) {
        val faction = textView.text.toString()
        players!![iter % players!!.size]!!.factions[iter / players!!.size] = faction
        factionAdapter!!.remove(faction)
        next()
    }


    internal fun doPick() {
        setContentView(pickListview)

        otherPick!!.visibility = if (iter >= players!!.size) View.VISIBLE else View.GONE
        otherPick!!.text = String.format("%s and:", players!![iter % players!!.size]!!.factions[0])
        fadeIn(pickListview!!)
    }

    internal fun done() {
        //TODO

        val intent = Intent(this, Summary::class.java)
        intent.putExtra("players", players)
        startActivity(intent)
        finish()
    }

    class Player : Parcelable {
        internal var name: String
        internal var factions = arrayOfNulls<String>(2)
        internal var mulligans: IntArray

        constructor(n: String, m1: Int, m2: Int) {
            name = n
            mulligans = intArrayOf(m1, m2)
        }

        protected constructor(`in`: Parcel) {
            name = `in`.readString()
            factions = `in`.createStringArray()
            mulligans = `in`.createIntArray()
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(name)
            dest.writeStringArray(factions)
            dest.writeIntArray(mulligans)
        }

        companion object CREATOR : Parcelable.Creator<Player> {
            override fun createFromParcel(parcel: Parcel): Player {
                return Player(parcel)
            }

            override fun newArray(size: Int): Array<Player?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        internal val FORCE_MULLIGAN = 1
        internal val FORCE_PICK = 2
        internal val UNDO = 3
    }
}
