package com.qweex.smashuphelper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.*


class ImportDialog() {
    val layoutId = R.layout.import_dialog

    fun show(activity: ChangeFactions) {
        var dialog : AlertDialog? = null
        val layout = activity.layoutInflater.inflate(layoutId, null) as LinearLayout
        val serviceSpinner = layout.findViewById(R.id.services) as Spinner
        val importButton = layout.findViewById(R.id.import_expansions) as Button
        val idText = layout.findViewById(R.id.idText) as TextView

        val idGroup = layout.findViewById(R.id.idGroup)
        val usernameGroup = layout.findViewById(R.id.usernameGroup)
        val urlGroup = layout.findViewById(R.id.urlGroup)

        ////////// Fill field values from Cache
        val username = (layout.findViewById(R.id.username) as EditText)
        val id = (layout.findViewById(R.id.id) as EditText)
        val url = (layout.findViewById(R.id.url) as EditText)
        username.setText(loadFromCache(activity, ExpansionsLoader.USERNAME_KEY))
        id.setText(loadFromCache(activity, ExpansionsLoader.ID_KEY))
        url.setText(loadFromCache(activity, ExpansionsLoader.URL_KEY))

        ////////// Select adapter (for changing text size)
        val services: Array<String> = activity.getResources().getStringArray(R.array.services)
        val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(activity, R.layout.spinner_item, services)
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item)
        serviceSpinner.setAdapter(spinnerArrayAdapter)

        ////////// Select on item select listener
        serviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val service = serviceSpinner.selectedItem.toString()
                if (service.contains(ChangeFactions.BITBUCKET)) {
                    idGroup.visibility = View.VISIBLE
                    usernameGroup.visibility = View.VISIBLE
                    urlGroup.visibility = View.GONE
                } else if (service.contains(ChangeFactions.GITHUB)) {
                    idGroup.visibility = View.VISIBLE
                    usernameGroup.visibility = View.VISIBLE
                    urlGroup.visibility = View.GONE
                } else if (service.contains(ChangeFactions.GITLAB)) {
                    idGroup.visibility = View.VISIBLE
                    usernameGroup.visibility = View.GONE
                    urlGroup.visibility = View.GONE
                } else if(service.equals("URL")) {
                    idGroup.visibility = View.GONE
                    usernameGroup.visibility = View.GONE
                    urlGroup.visibility = View.VISIBLE
                } else {
                    throw Exception("Unknown Service $service")
                }
                idText.text = service.split(" ".toRegex()).last() + " ID"
            }
            override fun onNothingSelected(adapterView: AdapterView<*>) {
                //idText.text = "ID"
            }
        }

        ////////// Select starting value
        val defaultService = loadFromCache(activity, ExpansionsLoader.SERVICE_KEY) as String?
        Log.d("!!!", "defaultService " + defaultService)
        if(defaultService != null) {
            var iter = 0
            for(service in services) {
                Log.d("!!!", "service " + service)
                if(service.equals(defaultService)) {
                    Log.d("!!!", "selected: " + iter)
                    serviceSpinner.setSelection(iter)
                    break
                }
                iter++
            }
        }

        ////////// Import Button
        importButton.setOnClickListener(View.OnClickListener {
            val service = serviceSpinner.selectedItem.toString()
            Log.d("!!!", service)
            if (service === "URL") {
                activity.performImport(url.text.toString())
            } else {
                activity.performImport(service, username.text.toString(), id.text.toString())
            }
            dialog!!.hide()
        })

        dialog = AlertDialog.Builder(activity)
                .setView(layout)
                .show()
    }

    internal fun loadFromCache(activity: Activity, key : String) : String? {
        Log.d("!!!", "loadFrom Cache")
        val sharedPreferences = activity!!.getSharedPreferences(ExpansionsLoader.SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }
}