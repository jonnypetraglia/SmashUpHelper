package com.qweex.smashuphelper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ExpandableListView
import android.widget.Toast
import org.json.JSONException
import java.io.IOException
import java.util.*

class ChangeFactions : AppCompatActivity(), OnCloseNotice{
    internal var listView: ExpandableListView? = null

    private val onClickImport = MenuItem.OnMenuItemClickListener {
        if (!hasInternetPermission()) {
            // 1. Request Internet permission
            requestInternetPermission()
            return@OnMenuItemClickListener false
        } else {
            // 2. If granted, show popup with dependent picklist:
            //   - Github Gist (snippedID)
            //   - Bitbucket Snippet (snippedID)
            //   - Gitlab Snippet (snippedID)
            //   - Have checkbox that says "I understand this will reset my settings"
            showImport()
        }
        false
    }

    private val onClickResetExpansions = MenuItem.OnMenuItemClickListener {
        val builder = AlertDialog.Builder(this@ChangeFactions)
        builder.setMessage("Are you sure?").setNegativeButton("No", null).setPositiveButton("Yes") { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                val loader = ExpansionsLoader(this@ChangeFactions)
                try {
                    this.OnCloseNotice(loader.loadFromAsset() as Object)
                } catch (error: Exception) {
                    showError(MSG_ASSET_ERROR)
                }
            }
        }.show()
        false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listView = ExpandableListView(this)
        setContentView(listView)
        val expansions = this.intent.getSerializableExtra("expansions")  as ArrayList<Expansion>?
        if(expansions != null) {
            Log.d("!!!!", "expansions")
            Log.d("!!!!", expansions.toString())
            reSetAdapter(expansions)
        }
    }
    internal fun reSetAdapter(expansions : ArrayList<Expansion>) {
        val adapter = ExpansionExpandableAdapter(expansions)
        listView!!.setAdapter(adapter)
        listView!!.setGroupIndicator(null)
        listView!!.setOnGroupClickListener(adapter)
        listView!!.setOnChildClickListener(adapter)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bar, menu)
        menu.findItem(R.id.import_expansions).setOnMenuItemClickListener(onClickImport)
        menu.findItem(R.id.reset_expansions).setOnMenuItemClickListener(onClickResetExpansions)
        return true
    }
    internal fun requestInternetPermission() {
        ActivityCompat.requestPermissions(this@ChangeFactions, arrayOf(Manifest.permission.INTERNET), 42)
    }
    internal fun hasInternetPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
    }
    override fun OnCloseNotice(returnVal: Object) {
        val i = Intent()
        i.putExtra("expansions", returnVal as ArrayList<Expansion>)
        setResult(Activity.RESULT_OK, i)
        reSetAdapter(returnVal as ArrayList<Expansion>)
    }

    fun importFailure(error : Exception) {
        SimpleNotice(this).showError(error, null)
    }

    fun importSuccess(expansions : ArrayList<Expansion>) {
        val expansionsCount = expansions.size
        var factionsCount : Int = 0
        for(exp in expansions) {
            factionsCount += exp.factions.size
        }
        SimpleNotice(this).show("Imported " + factionsCount + " factions from " + expansionsCount + " expansions", expansions, this)
    }

    internal fun showImport() {
        val dialog = ImportDialog()
        dialog.show(this)
    }

    internal fun performImport(url: String) {
        val loader = ExpansionsLoader(this)
        try {
            loader.loadFromURL(url)
            loader.saveUrlToCache(url)
        } catch (e: JSONException) {
            showError("JSONException")
            e.printStackTrace()
        } catch (e: IOException) {
            showError("IOException")
            e.printStackTrace()
        }
    }

    internal fun performImport(service: String, username: String, id: String) {
        val loader = ExpansionsLoader(this)
        Log.d("!!!", "performImport " + service)
        try {
            if (service.contains(BITBUCKET)) {
                loader.loadFromBitbucketSnippet(username, id)
            } else if (service.contains(GITHUB)) {
                loader.loadFromGithubGist(username, id)
            } else if (service.contains(GITLAB)) {
                loader.loadFromGitlabSnippet(id)
            } else {
                throw Exception("Unknown Service $service")
            }
            loader.saveServiceToCache(service, username, id)
        } catch (error: Exception) {
            Log.d("WHAT", error.toString())
            // 5d. If fail, show error notice
            showError(MSG_IMPORT_FAILED + ": " + error.message)
        }

    }

    private fun showError(msg: String) {
        Toast.makeText(this@ChangeFactions, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Checking whether user granted the permission or not.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showImport()
        } else {
            showError(MSG_PERMISSION_REQUIRED)
        }
    }

    internal inner class ExpansionExpandableAdapter(var expansions: ArrayList<Expansion>) : BaseExpandableListAdapter(), ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

        init {
            val i = Intent()
            i.putExtra("expansions", expansions)
            setResult(Activity.RESULT_OK, i)
        }

        override fun getGroupCount(): Int {
            return expansions.size
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return expansions[groupPosition].factions.size
        }

        override fun getGroup(groupPosition: Int): Expansion {
            return expansions[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Faction {
            return expansions[groupPosition].factions[childPosition]!!
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return (groupPosition * 100000 + childPosition).toLong()
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.expansion, null)
            if (!getGroup(groupPosition).isDisabled) {
                if (!this@ChangeFactions.listView!!.isGroupExpanded(groupPosition))
                    this@ChangeFactions.listView!!.expandGroup(groupPosition)
            } else if (this@ChangeFactions.listView!!.isGroupExpanded(groupPosition))
                this@ChangeFactions.listView!!.collapseGroup(groupPosition)


            val check = convertView!!.findViewById(android.R.id.checkbox) as CheckBox
            Log.d("getGroupView", (!getGroup(groupPosition).isDisabled).toString() + " " + getGroup(groupPosition).name)
            check.isChecked = !getGroup(groupPosition).isDisabled
            check.text = getGroup(groupPosition).name
            return convertView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.faction, null)
            val check = convertView!!.findViewById(android.R.id.checkbox) as CheckBox
            check.text = getChild(groupPosition, childPosition).name
            check.isChecked = !getChild(groupPosition, childPosition).isDisabled
            check.isEnabled = !getGroup(groupPosition).isDisabled
            return convertView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun onGroupClick(parent: ExpandableListView, v: View, groupPosition: Int, id: Long): Boolean {
            Log.d("ChangeFactions", "onGroupClick")
            getGroup(groupPosition).isDisabled = !getGroup(groupPosition).isDisabled
            if (!getGroup(groupPosition).isDisabled) {
                if (!parent.isGroupExpanded(groupPosition))
                    parent.expandGroup(groupPosition)
            } else if (parent.isGroupExpanded(groupPosition))
                parent.collapseGroup(groupPosition)

            val check = v.findViewById(android.R.id.checkbox) as CheckBox
            check.isChecked = !getGroup(groupPosition).isDisabled
            return true
        }

        override fun onChildClick(parent: ExpandableListView, v: View, groupPosition: Int, childPosition: Int, id: Long): Boolean {
            Log.d("ChangeFactions", "onChildClick")
            getChild(groupPosition, childPosition).isDisabled = !getChild(groupPosition, childPosition).isDisabled

            val check = v.findViewById(android.R.id.checkbox) as CheckBox
            check.isChecked = !getChild(groupPosition, childPosition).isDisabled
            return true
        }
    }

    companion object {
        internal val MSG_PERMISSION_REQUIRED = "You must grant the Internet Permission to import from online services."
        internal val MSG_ASSET_ERROR = "An error occurred trying to reset the Expansions. That is definitely not a good thing."
        internal val MSG_IMPORT_FAILED = "Import Failed"
        internal val BITBUCKET = "Bitbucket"
        internal val GITHUB = "Github"
        internal val GITLAB = "Gitlab"
    }
}
