package com.qweex.smashuphelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;

import java.util.ArrayList;


public class ChangeFactions extends AppCompatActivity  {

    ExpandableListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(listView = new ExpandableListView(this));

        ArrayList<Expansion> expansions = getIntent().<Expansion>getParcelableArrayListExtra("expansions");

        ExpansionExpandableAdapter adapter = new ExpansionExpandableAdapter(expansions);
        listView.setAdapter(adapter);
        listView.setGroupIndicator(null);
        listView.setOnGroupClickListener(adapter);
        listView.setOnChildClickListener(adapter);
    }


    class ExpansionExpandableAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
        ArrayList<Expansion> expansions;

        public ExpansionExpandableAdapter(ArrayList<Expansion> expansions) {
            super();
            this.expansions = expansions;
            Intent i = new Intent();
            i.putExtra("expansions", expansions);
            setResult(RESULT_OK, i);
        }

        @Override
        public int getGroupCount() {
            return expansions.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return expansions.get(groupPosition).factions.length;
        }

        @Override
        public Expansion getGroup(int groupPosition) {
            return expansions.get(groupPosition);
        }

        @Override
        public Faction getChild(int groupPosition, int childPosition) {
            return expansions.get(groupPosition).factions[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return (groupPosition * 100000) + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView==null)
                convertView = getLayoutInflater().inflate(R.layout.expansion, null);
            if(!getGroup(groupPosition).isDisabled()) {
                if (!ChangeFactions.this.listView.isGroupExpanded(groupPosition))
                    ChangeFactions.this.listView.expandGroup(groupPosition);
            } else
                if(ChangeFactions.this.listView.isGroupExpanded(groupPosition))
                    ChangeFactions.this.listView.collapseGroup(groupPosition);


            CheckBox check = (CheckBox)convertView.findViewById(android.R.id.checkbox);;
            Log.d("getGroupView", !getGroup(groupPosition).isDisabled() + " " + getGroup(groupPosition).name);
            check.setChecked(!getGroup(groupPosition).isDisabled());
            check.setText(getGroup(groupPosition).name);
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView==null)
                convertView = getLayoutInflater().inflate(R.layout.faction, null);
            CheckBox check = (CheckBox)convertView.findViewById(android.R.id.checkbox);;
            check.setText(getChild(groupPosition, childPosition).name);
            check.setChecked(!getChild(groupPosition, childPosition).isDisabled());
            check.setEnabled(!getGroup(groupPosition).isDisabled());
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            Log.d("ChangeFactions", "onGroupClick");
            getGroup(groupPosition).setDisabled(!getGroup(groupPosition).isDisabled());
            if(!getGroup(groupPosition).isDisabled()) {
                if (!parent.isGroupExpanded(groupPosition))
                    parent.expandGroup(groupPosition);
            } else
                if (parent.isGroupExpanded(groupPosition))
                    parent.collapseGroup(groupPosition);

            CheckBox check = (CheckBox)v.findViewById(android.R.id.checkbox);
            check.setChecked(!getGroup(groupPosition).isDisabled());
            return true;
        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            Log.d("ChangeFactions", "onChildClick");
            getChild(groupPosition, childPosition).setDisabled(!getChild(groupPosition, childPosition).isDisabled());

            CheckBox check = (CheckBox)v.findViewById(android.R.id.checkbox);
            check.setChecked(!getChild(groupPosition, childPosition).isDisabled());
            return true;
        }
    }
}
