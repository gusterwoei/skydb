package com.guster.sqlitecreator.sample;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.guster.sqlitecreator.sample.domain.Attendance;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;
import com.guster.sqlitecreator.sample.list.StandardListAdapter;

import java.util.List;

/**
 * Created by Gusterwoei on 3/18/15.
 *
 */
public class NewAttendanceActivity extends BaseActivity implements View.OnClickListener {
    private Spinner studentSpinner;
    private Spinner lecturerSpinner;
    private Spinner subjectSpinner;
    private MySpinnerAdapter<Student> studentAdapter;
    private MySpinnerAdapter<Lecturer> lecturerAdapter;
    private MySpinnerAdapter<Subject> subjectAdapter;
    private Button btnConfirm;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_attendance);
        studentSpinner = (Spinner) findViewById(R.id.spinner_student);
        lecturerSpinner = (Spinner) findViewById(R.id.spinner_lecturer);
        subjectSpinner = (Spinner) findViewById(R.id.spinner_subject);
        btnConfirm = (Button) findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);

        btnConfirm.setOnClickListener(this);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        loadFormData();
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

    private void loadFormData() {
        new AsyncTask<Void, Void, List<Student>>() {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected List<Student> doInBackground(Void... voids) {
                return getStudentRepository().findAll();
            }

            @Override
            protected void onPostExecute(List<Student> students) {
                progressBar.setVisibility(View.GONE);
                setFormData(students);
            }
        }.execute();

    }

    private void setFormData(List<Student> students) {
        studentAdapter = new MySpinnerAdapter<Student>(getApplicationContext(), students) {
            @Override
            public View getView(int i, Student item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
                txtTitle.setText(item.getFirstName() + " " + item.getLastName());
                return view;
            }

            @Override
            public View getDropDownView(int position, Student item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
                txtTitle.setText(item.getFirstName() + " " + item.getLastName());
                return view;
            }
        };
        studentSpinner.setAdapter(studentAdapter);

        List<Lecturer> lecturers = getLecturerRepository().findAll();
        lecturerAdapter = new MySpinnerAdapter<Lecturer>(getApplicationContext(), lecturers) {
            @Override
            public View getView(int i, Lecturer item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
                txtTitle.setText(item.getFirstName() + " " + item.getLastName());
                return view;
            }

            @Override
            public View getDropDownView(int position, Lecturer item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
                txtTitle.setText(item.getFirstName() + " " + item.getLastName());
                return view;
            }
        };
        lecturerSpinner.setAdapter(lecturerAdapter);

        List<Subject> subjects = getSubjectRepository().findAll();
        subjectAdapter = new MySpinnerAdapter<Subject>(getApplicationContext(), subjects) {
            @Override
            public View getView(int i, Subject item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
                txtTitle.setText(item.getTitle());
                return view;
            }

            @Override
            public View getDropDownView(int position, Subject item, View view, ViewGroup parent) {
                TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
                txtTitle.setText(item.getTitle());
                return view;
            }
        };
        subjectSpinner.setAdapter(subjectAdapter);
    }

    @Override
    public void onClick(View view) {
        if(view == btnConfirm) {
            Student student = (Student) studentSpinner.getSelectedItem();
            Lecturer lecturer = (Lecturer) lecturerSpinner.getSelectedItem();
            Subject subject = (Subject) subjectSpinner.getSelectedItem();

            long time = System.currentTimeMillis();
            Attendance attendance = new Attendance();
            attendance.setStudentId(student.getStudentId());
            attendance.setLecturerId(lecturer.getLecturerId());
            attendance.setSubjectId(subject.getSubjectId());
            attendance.setCreatedDate(time);
            attendance.setModifiedDate(time);
            getAttendanceRepository().save(attendance);

            setResult(RESULT_OK);
            finish();

            Toast.makeText(this, "New attendance added!", Toast.LENGTH_SHORT).show();
        }
    }


    private abstract class MySpinnerAdapter<T> extends StandardListAdapter<T> {

        public MySpinnerAdapter(Context context, List<T> data) {
            super(context, R.layout.listitem_simple, data);
        }

        public abstract View getDropDownView(int position, T item, View view, ViewGroup parent);

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.listitem_simple, parent, false);
            }

            getDropDownView(position, getItem(position), view, parent);

            return view;
        }

        @Override
        public String getFilterCriteria(T item, CharSequence userInput) {
            return null;
        }

        @Override
        public String getFilterResultText(T item, CharSequence userInput) {
            return null;
        }

        @Override
        public Boolean getRegex(String compareValue, CharSequence userInput) {
            return null;
        }
    }
}
