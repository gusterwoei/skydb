package com.guster.skydb.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.guster.skydb.SkyDatabase;
import com.guster.skydb.sample.domain.Attendance;
import com.guster.skydb.sample.domain.Book;
import com.guster.skydb.sample.domain.Lecturer;
import com.guster.skydb.sample.domain.Student;
import com.guster.skydb.sample.domain.Subject;

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
        helper.createTable(Book.class);
    }

    @Override
    public void onMigrate(SQLiteDatabase db, int version, DatabaseHelper helper) {
        Log.d("SKYDB", "ON MIGRATE -> " + version);

        try {
            switch (version) {
                case 1:
                    break;
                case 2:
                    helper.addAllColumns(Student.class);
                    /*SqlBuilder query = SqlBuilder.newInstance()
                            .alterTable(Student.TABLE_NAME)
                            .addColumn("status", "integer")
                            .build();
                    db.execSQL(query.getQuery());*/
                    break;
                // and so on...
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("SKYDB", "Alter table exception: " + e.getMessage());
        }
    }
}
