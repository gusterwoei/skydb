package com.guster.sqlitecreator.sample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.guster.sqlitecreator.sample.dao.AttendanceRepository;
import com.guster.sqlitecreator.sample.dao.LecturerRepository;
import com.guster.sqlitecreator.sample.dao.StudentRepository;
import com.guster.sqlitecreator.sample.dao.SubjectRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by kellylu on 3/18/15.
 */
public class BaseActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private StudentRepository studentRepository;
    private LecturerRepository lecturerRepository;
    private SubjectRepository subjectRepository;
    private AttendanceRepository attendanceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        studentRepository = new StudentRepository(getApplicationContext());
        lecturerRepository = new LecturerRepository(getApplicationContext());
        subjectRepository = new SubjectRepository(getApplicationContext());
        attendanceRepository = new AttendanceRepository(getApplicationContext());
    }

    @Override
    public void setContentView(int layoutResID) {
        View toolbarView = getLayoutInflater().inflate(R.layout.toolbar, null);
        ViewGroup root = (ViewGroup) getLayoutInflater().inflate(layoutResID, null);
        root.addView(toolbarView, 0);
        setContentView(root);

        // set toolbar as the action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public StudentRepository getStudentRepository() {
        return studentRepository;
    }

    public LecturerRepository getLecturerRepository() {
        return lecturerRepository;
    }

    public SubjectRepository getSubjectRepository() {
        return subjectRepository;
    }

    public AttendanceRepository getAttendanceRepository() {
        return attendanceRepository;
    }

    public void saveDbToSdCard(String dbName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + getPackageName() + "/databases/" + dbName;
                String backupDBPath = dbName;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Log.d("SQLCREATOR", e.getMessage());
        }
    }
}
