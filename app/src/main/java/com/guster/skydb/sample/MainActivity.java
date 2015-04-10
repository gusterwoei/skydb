package com.guster.skydb.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.guster.skydb.sample.domain.Attendance;
import com.guster.skydb.sample.list.StandardListAdapter;
import java.util.List;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView listData;
    private StandardListAdapter<Attendance> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.guster.skydb.sample.R.layout.activity_main);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        listData = (ListView) findViewById(com.guster.skydb.sample.R.id.list_data);

        setFormData();
    }

    private void setFormData() {
        // get data
        List<Attendance> attendances = getAttendanceRepository().findAll();

        listAdapter = new StandardListAdapter<Attendance>(getApplicationContext(), com.guster.skydb.sample.R.layout.listitem_item, attendances) {
            @Override
            public View getView(int i, Attendance item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(com.guster.skydb.sample.R.id.txt_title);
                TextView txtStudent = (TextView) view.findViewById(com.guster.skydb.sample.R.id.txt_student);
                TextView txtLecturer = (TextView) view.findViewById(com.guster.skydb.sample.R.id.txt_lecturer);
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
        listData.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final Attendance att = listAdapter.getItem(i);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Delete Attendance");
        dialog.setMessage("Do you want to delete " + att.getSubjectName() + ", taught by " + att.getLecturerName() +
        ", attended by " + att.getStudentName() + "?");
        dialog.setNegativeButton("Cancel", null);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getAttendanceRepository().delete(att);
                setFormData();
            }
        });
        dialog.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.guster.skydb.sample.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.guster.skydb.sample.R.id.action_settings) {
            return true;
        }
        else if(id == com.guster.skydb.sample.R.id.action_debug) {
            saveDbToSdCard("sqliteCreator.db");
        }
        else if(id == com.guster.skydb.sample.R.id.action_add) {
            startActivityForResult(new Intent(this, NewAttendanceActivity.class), 99);
        }
        else if(id == com.guster.skydb.sample.R.id.action_delete) {
            Log.d("ABC", "Deleting all subjects...");
            getSubjectRepository().deleteAll();
        }
        else if(id == com.guster.skydb.sample.R.id.action_insert) {
            Log.d("ABC", "Inserting all subjects...");
            DataContentProvider.testDbInsertPerformance(getApplicationContext());
        }
        else if(id == com.guster.skydb.sample.R.id.action_fetch) {
            Log.d("ABC", "Fetching all subjects...");
            long time1 = System.currentTimeMillis();
            getSubjectRepository().findAll();
            long time2 = System.currentTimeMillis();
            Log.d("ABC", "Fetch time = " + (time2 - time1));
        }
        else if(id == com.guster.skydb.sample.R.id.action_analyze) {
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
