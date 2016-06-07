package com.joe.app.outbound;

import android.app.Application;

/**
 * Created by ZDD on 2016/6/4.
 */
public class MyApplication extends Application{
    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance(){
        return instance;
    }
}
