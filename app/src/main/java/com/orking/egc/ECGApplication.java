package com.orking.egc;

import android.app.Application;

import com.orking.egc.utils.L;

/**
 * Created by zhanglei on 2017/3/1.
 */

public class ECGApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        L.isDebug = true;
    }
}
