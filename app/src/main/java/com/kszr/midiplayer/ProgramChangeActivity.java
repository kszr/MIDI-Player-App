package com.kszr.midiplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhishekchatterjee on 12/22/15.
 */
public class ProgramChangeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_list);
        setUpProgramList();
    }

    /**
     * Sets up the program list.
     */
    private void setUpProgramList() {
        List<String> programList = getProgramList();
        final ListView listView = (ListView) findViewById(R.id.program_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, programList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                String instrument = (String) (listView.getItemAtPosition(myItemInt));
                int program = myItemInt + 1;
                Log.i("ProgramChangeActivity", "Instrument: " + instrument + ", Program: " + program);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("instrument", instrument);
                intent.putExtra("program", program);
                if (getParent() == null) {
                    setResult(RESULT_OK, intent);
                } else {
                    getParent().setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
        listView.setAdapter(arrayAdapter);
    }

    /**
     * Return a list of all available program names ordered by their program number.
     * @return A list of all available program names.
     */
    private List<String> getProgramList() {
        List<String> programList = new ArrayList<String>();
        programList.add("Piano");
        return programList;
    }
}
