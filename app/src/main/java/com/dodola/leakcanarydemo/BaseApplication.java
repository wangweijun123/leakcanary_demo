package com.dodola.leakcanarydemo;

import android.app.Activity;
import android.app.Application;
import android.os.StrictMode;

import com.dodola.leakcanarydemo.leakcanary.LeakCanary;

import java.util.ArrayList;
import java.util.List;


public class BaseApplication extends Application {
    private List<Activity> mCurrentActivity = new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();

        setupLeakCanary();
    }

    protected void setupLeakCanary() {
        /*enabledStrictMode();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }*/
        LeakCanary.install(this);
    }

    private static void enabledStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build());
    }

    public void addCurrentActivity(Activity activity){
        mCurrentActivity.add(activity);
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity.get(0);
    }
}

