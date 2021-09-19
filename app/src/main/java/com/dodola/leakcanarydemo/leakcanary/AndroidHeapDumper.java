/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dodola.leakcanarydemo.leakcanary;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dodola.leakcanarydemo.R;

import java.io.File;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class AndroidHeapDumper implements HeapDumper {

  private final Context context;
  private final LeakDirectoryProvider leakDirectoryProvider;
  private final Handler mainHandler;


  public AndroidHeapDumper(@NonNull Context context,
                           @NonNull LeakDirectoryProvider leakDirectoryProvider) {
    this.leakDirectoryProvider = leakDirectoryProvider;
    this.context = context.getApplicationContext();
    mainHandler = new Handler(Looper.getMainLooper());
  }

  @SuppressWarnings("ReferenceEquality") // Explicitly checking for named null.
  @Override @Nullable
  public File dumpHeap() {
//    File heapDumpFile = leakDirectoryProvider.newHeapDumpFile();
    File heapDumpFile = new File(context.getCacheDir(), "test.hprof");
    if (heapDumpFile.exists()) {
      heapDumpFile.delete();
    }
    try {
      heapDumpFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }


    FutureResult<Toast> waitingForToast = new FutureResult<>();
    showToast(waitingForToast);

    try {
      Debug.dumpHprofData(heapDumpFile.getAbsolutePath());
      return heapDumpFile;
    } catch (Exception e) {
      CanaryLog.d(e, "Could not dump heap");
      // Abort heap dump
      return RETRY_LATER;
    }
  }

  private void showToast(final FutureResult<Toast> waitingForToast) {
    mainHandler.post(new Runnable() {
      @Override public void run() {
      }
    });
  }

  private void cancelToast(final Toast toast) {
    if (toast == null) {
      return;
    }
    mainHandler.post(new Runnable() {
      @Override public void run() {
        toast.cancel();
      }
    });
  }
}
