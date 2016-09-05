package com.guster.skydb;

import android.util.Log;

/**
 * Created by Gusterwoei on 12/17/15.
 */
class Util {
    public static void logd(String msg) {
        Log.d("SKYDB", msg);
    }

    public static void loge(String msg) {
        Log.e("SKYDB", msg);
    }

    public static void loge(String msg, Exception e) {
        Log.e("SKYDB", msg, e);
    }
}
