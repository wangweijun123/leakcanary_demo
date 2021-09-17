package com.dodola.leakcanarydemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        BaseApplication application = (BaseApplication) getApplication();
        application.addCurrentActivity(this);

//        startThread();
    }

    public void click(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    private void startThread() {
        // 对象是永久泄漏, 还是只有这一段时间
        // 匿名内部类, 拥有外部类的引用
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("wangweijun", "进入睡眠");
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("wangweijun", "睡醒了");
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 但是对象不一定会被释放，其实我们肯定是希望回收
    }
}