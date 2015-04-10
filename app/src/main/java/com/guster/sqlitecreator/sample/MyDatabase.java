package com.guster.sqlitecreator.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.guster.sqlitecreator.SkyDatabase;
import com.guster.sqlitecreator.SqlBuilder;
import com.guster.sqlitecreator.sample.domain.Attendance;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class MyDatabase extends SkyDatabase {

    public MyDatabase(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db, DatabaseHelper helper) {
        helper.createTable(Student.class);
        helper.createTable(Lecturer.class);
        helper.createTable(Subject.class);
        helper.createTable(Attendance.class);
    }

    @Override
    public void onMigrate(SQLiteDatabase db, int version, DatabaseHelper helper) {
        switch(version) {
            case 1:
                break;
            case 2:
                SqlBuilder query = SqlBuilder.newInstance()
                        .alterTable(Student.TABLE_NAME)
                        .addColumn("status", "integer")
                        .build();
                db.execSQL(query.getQuery());
                break;
            // and so on...
        }
    }
}
