package com.dodola.leakcanarydemo;

import android.app.Activity;
import android.app.Application;
import java.util.ArrayList;
import java.util.List;


public class BaseApplication extends Application {
    private List<Activity> mCurrentActivity = new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void addCurrentActivity(Activity activity){
        mCurrentActivity.add(activity);
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity.get(0);
    }
}

