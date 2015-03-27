package com.guster.sqlitecreator.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.guster.sqlitecreator.DatabaseCreator;
import com.guster.sqlitecreator.sample.domain.Attendance;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class MyDatabase extends DatabaseCreator {

    public MyDatabase(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, databaseVersion);
    }

    @Override
    protected DatabaseCreator getDatabase() {
        return this;
    }

    @Override
    public void onOpenDb(SQLiteDatabase db) {
        // your logic
    }

    @Override
    public void onCreateDb(SQLiteDatabase db) {
        createSchemaFor(Student.class, db);
        createSchemaFor(Lecturer.class, db);
        createSchemaFor(Subject.class, db);
        createSchemaFor(Attendance.class, db);
    }

    @Override
    public void onUpgradeDb(SQLiteDatabase db, int newVersion) {
        switch(newVersion) {
            case 1:
                break;
            case 2:
                break;
            // and so on...
        }
    }
}
