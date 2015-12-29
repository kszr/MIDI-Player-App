package com.kszr.midiplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.kszr.midiplayer.constant.ProgramList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An activity that lists available programs.
 * Created by abhishekchatterjee on 12/22/15.
 */
public class ProgramChangeActivity extends AppCompatActivity {
    private EditText inputSearch;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_list);
        setUpProgramList();
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        addInputTextChangedListener();
    }

    private void addInputTextChangedListener() {
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                arrayAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * Sets up the program list.
     */
    private void setUpProgramList() {
        List<String> programList = getProgramList();
        final ListView listView = (ListView) findViewById(R.id.program_list);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, programList);
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
     * Returns a list of all available program names ordered by their program number.
     * @return A list of all available program names.
     */
    private List<String> getProgramList() {
        return new ArrayList<>(Arrays.asList(ProgramList.programList));
    }
}
