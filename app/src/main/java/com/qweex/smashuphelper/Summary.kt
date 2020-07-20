package com.qweex.smashuphelper

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView


class Summary : AppCompatActivity() {
    internal var players: Array<Selecting.Player?>? = null
    internal var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pp = intent.getParcelableArrayExtra("players")
        players = arrayOfNulls(pp.size)
        for (i in pp.indices)
            players!![i] = pp[i] as Selecting.Player

        listView = ListView(this)
        setContentView(listView)
        listView!!.adapter = SummaryAdapter(this, android.R.layout.simple_list_item_2, players!!)
    }

    internal inner class SummaryAdapter(context: Context, resource: Int, var players: Array<Selecting.Player?>) : ArrayAdapter<Selecting.Player>(context, resource, android.R.id.text1, players) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            convertView = super.getView(position, convertView, parent)
            convertView!!.isFocusable = false
            convertView.isClickable = false
            (convertView.findViewById(android.R.id.text2) as TextView).text = String.format("%s & %s", players[position]!!.factions[0], players[position]!!.factions[1])
            (convertView.findViewById(android.R.id.text1) as TextView).text = players[position]!!.name
            return convertView
        }
    }
}
