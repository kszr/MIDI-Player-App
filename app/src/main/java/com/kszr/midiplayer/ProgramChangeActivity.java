package com.kszr.midiplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        List<String> programList = new ArrayList<String>();
        programList.add("Piano");
        programList.add("Other instruments");
        ListView listView = (ListView) findViewById(R.id.program_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, programList);
        listView.setAdapter(arrayAdapter);
    }
}
