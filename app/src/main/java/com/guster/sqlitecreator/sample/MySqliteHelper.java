package com.guster.sqlitecreator.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.guster.sqlitecreator.InglabSQLiteOpenHelper;
import com.guster.sqlitecreator.sample.domain.Attendance;
import com.guster.sqlitecreator.sample.domain.Lecturer;
import com.guster.sqlitecreator.sample.domain.Student;
import com.guster.sqlitecreator.sample.domain.Subject;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class MySqliteHelper extends InglabSQLiteOpenHelper {
    public static final String DB_NAME = "sqliteCreator.db";
    public static final int DB_VERSION = 1;
    private static MySqliteHelper mySqliteHelper;

    private MySqliteHelper(Context context) {
        super(context, DB_NAME, DB_VERSION);
    }

    public static MySqliteHelper getInstance(Context context) {
        if(mySqliteHelper == null) {
            mySqliteHelper = new MySqliteHelper(context);
        }
        return mySqliteHelper;
    }

    @Override
    public void onOpenDb(SQLiteDatabase db) {

    }

    @Override
    public void onCreateDb(SQLiteDatabase db) {
        db.execSQL(createSchemaFor(Student.class));
        db.execSQL(createSchemaFor(Lecturer.class));
        db.execSQL(createSchemaFor(Subject.class));
        db.execSQL(createSchemaFor(Attendance.class));
    }

    @Override
    public void onUpgradeDb(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
