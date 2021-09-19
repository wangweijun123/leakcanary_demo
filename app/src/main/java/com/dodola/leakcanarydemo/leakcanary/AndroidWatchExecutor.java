package com.dodola.leakcanarydemo.leakcanary;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;

public class AndroidWatchExecutor implements WatchExecutor{
    private final Handler backgroundHandler;
    static final String LEAK_CANARY_THREAD_NAME = "LeakCanary-Heap-Dump";
    public AndroidWatchExecutor() {

        HandlerThread handlerThread = new HandlerThread(LEAK_CANARY_THREAD_NAME);
        // 开启线程后会准备好looper，进入loop()死循环
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    // 延时5秒钟从queue里面去取
    // 堆转存储: 把堆内存写入文件很耗时，5~8 s秒钟
    // Hprof 的解析也更加耗时 8~ 10 s
    // 需要等待主线程空闲
    @Override
    public void execute(Retryable retryable) {
        // 主线程
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.d("LeakCanary", "当前是主线程，addIdleHandler，等待主线程空闲");
            waitForIdle(retryable, 0);
        } else {// 子线程

        }
    }

    private void waitForIdle(Retryable retryable, int failedAttempts) {
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                // 主线程空闲你可以处理
                // 开启线程
                Log.d("LeakCanary", "当前是主线程空闲，开启子线程延时5s执行接口IdleHandler");
                postToBackgroundWithDelay(retryable, failedAttempts);
                return false;
            }
        });
    }

    private void postToBackgroundWithDelay(final Retryable retryable, final int failedAttempts) {
        backgroundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("LeakCanary", "回调执行接口 ");
                Retryable.Result run = retryable.run();
            }
        }, 5000);
    }
}
