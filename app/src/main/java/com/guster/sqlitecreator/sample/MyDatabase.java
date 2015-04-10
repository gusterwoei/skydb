package com.guster.sqlitecreator.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.guster.sqlitecreator.GusterDatabase;
import com.guster.sqlitecreator.SqlBuilder;
import com.guster.sqlitecreator.sample.domain.Attendance;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class MyDatabase extends GusterDatabase {

    public MyDatabase(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db, DatabaseHelper creator) {
        creator.createTable(Student.class);
        creator.createTable(Lecturer.class);
        creator.createTable(Subject.class);
        creator.createTable(Attendance.class);
    }

    @Override
    public void onMigrate(SQLiteDatabase db, int version, DatabaseHelper creator) {
        switch(version) {
            case 1:
                break;
            case 2:
                SqlBuilder query = SqlBuilder.newInstance();

                break;
            // and so on...
        }
    }
}
