package com.guster.sqlitecreator.sample;

import android.app.Application;
import android.util.Log;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // create db
        Log.d("SQLCREATOR", "create database");
        MySqliteHelper dbHelpder = MySqliteHelper.getInstance(this);
        dbHelpder.openDatabase();

        // load dummy data
        Log.d("SQLCREATOR", "load dummy data");
        DataContentProvider.loadDummyData(this);
    }
}
