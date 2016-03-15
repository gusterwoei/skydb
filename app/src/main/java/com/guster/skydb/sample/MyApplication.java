package com.guster.skydb.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Gusterwoei on 3/17/15.
 *
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // create db
        MyDatabase myDatabase = new MyDatabase(this, getString(R.string.db_name), Integer.parseInt(getString(R.string.db_version)));
        myDatabase.createDatabase();

        // load dummy data
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = sp.getBoolean("first_time", true);
        if(firstTime) {
            DataContentProvider.loadDummyData(this);

            sp.edit().putBoolean("first_time", false).apply();
        }
    }
}
