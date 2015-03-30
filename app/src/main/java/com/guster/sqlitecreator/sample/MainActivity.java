package com.guster.sqlitecreator.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.guster.sqlitecreator.sample.domain.Attendance;
import com.guster.sqlitecreator.sample.domain.Subject;
import com.guster.sqlitecreator.sample.list.BaseViewHolder;
import com.guster.sqlitecreator.sample.list.StandardListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ListView listData;
    private StandardListAdapter<Attendance> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        listData = (ListView) findViewById(R.id.list_data);

        setFormData();
    }

    private void setFormData() {
        // get data
        List<Attendance> attendances = getAttendanceRepository().findAll();

        listAdapter = new StandardListAdapter<Attendance>(getApplicationContext(), R.layout.listitem_item, attendances) {
            @Override
            public View getView(int i, Attendance item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
                TextView txtStudent = (TextView) view.findViewById(R.id.txt_student);
                TextView txtLecturer = (TextView) view.findViewById(R.id.txt_lecturer);
                txtTitle.setText(item.getSubjectName());
                txtStudent.setText(item.getStudentName());
                txtLecturer.setText(item.getLecturerName());
                return view;
            }
            @Override
            public String getFilterCriteria(Attendance item, CharSequence userInput) {return null;}
            @Override
            public String getFilterResultText(Attendance item, CharSequence userInput) {return null;}
            @Override
            public Boolean getRegex(String compareValue, CharSequence userInput) {return null;}
        };

        listData.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_debug) {
            saveDbToSdCard("sqliteCreator.db");
        }
        else if(id == R.id.action_add) {
            startActivityForResult(new Intent(this, NewAttendanceActivity.class), 99);
        }
        else if(id == R.id.action_delete) {
            Log.d("ABC", "Deleting all subjects...");
            getSubjectRepository().deleteAll();
        }
        else if(id == R.id.action_insert) {
            Log.d("ABC", "Inserting all subjects...");
            DataContentProvider.testDbInsertPerformance(getApplicationContext());
        }
        else if(id == R.id.action_fetch) {
            Log.d("ABC", "Fetching all subjects...");
            long time1 = System.currentTimeMillis();
            getSubjectRepository().findAll();
            long time2 = System.currentTimeMillis();
            Log.d("ABC", "Fetch time = " + (time2 - time1));
        }
        else if(id == R.id.action_analyze) {
            Log.d("ABC", "Running analyze");
            getSubjectRepository().runQuery("ANALYZE");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == 99) {
                setFormData();
            }
        }
    }
}
