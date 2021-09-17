package com.dodola.leakcanarydemo;

import android.util.Log;

public class TaskManager {

    interface Callback {
        void onSuccess();
        void onFailed();
    }

    public void doTask(Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("wangweijun", "开始干活");
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("wangweijun", "干活完毕");
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        }).start();
    }
}
