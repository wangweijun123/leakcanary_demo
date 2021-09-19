package com.dodola.leakcanarydemo.leakcanary;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.dodola.leakcanarydemo.leakcanary.Preconditions.checkNotNull;
import static com.dodola.leakcanarydemo.leakcanary.Retryable.Result.DONE;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class RefWatcher {
    public static final RefWatcher DISABLED = new RefWatcherBuilder<>().build();

    private final WatchExecutor watchExecutor;
    private final DebuggerControl debuggerControl;
    private final GcTrigger gcTrigger;
    private final HeapDumper heapDumper;
    private final HeapDump.Listener heapdumpListener;
    private final HeapDump.Builder heapDumpBuilder;
    private final Set<String> retainedKeys;
    private final ReferenceQueue<Object> queue;

    RefWatcher(WatchExecutor watchExecutor, DebuggerControl debuggerControl, GcTrigger gcTrigger,
               HeapDumper heapDumper, HeapDump.Listener heapdumpListener, HeapDump.Builder heapDumpBuilder) {
        this.watchExecutor = checkNotNull(watchExecutor, "watchExecutor");
        this.debuggerControl = checkNotNull(debuggerControl, "debuggerControl");
        this.gcTrigger = checkNotNull(gcTrigger, "gcTrigger");
        this.heapDumper = checkNotNull(heapDumper, "heapDumper");
        this.heapdumpListener = checkNotNull(heapdumpListener, "heapdumpListener");
        this.heapDumpBuilder = heapDumpBuilder;
        retainedKeys = new CopyOnWriteArraySet<>();
        queue = new ReferenceQueue<>();
    }


    public void watch(Object watchedReference) {
        String key = UUID.randomUUID().toString();
        retainedKeys.add(key);
        KeyedWeakReference reference = new KeyedWeakReference(key, "", watchedReference, queue);
        final long watchStartNanoTime = System.nanoTime();
        ensureGoneAsync(watchStartNanoTime, reference);
    }

    private void ensureGoneAsync(long watchStartNanoTime, KeyedWeakReference reference) {
        // 延时5秒钟从queue里面去取
        // 堆转存储: 把堆内存写入文件很耗时，5~8 s秒钟
        // Hprof 的解析也更加耗时 8~ 10 s
        // 需要等待主线程空闲
        // 其实不考虑retry的话，就是一个runnable好了
        watchExecutor.execute(new Retryable() {
            @Override
            public Result run() {
                return ensureGone(reference, watchStartNanoTime);
            }
        });


    }
    Retryable.Result ensureGone(final KeyedWeakReference reference, final long watchStartNanoTime) {
        out.println("LeakCanary ensureGone ...");
        long gcStartNanoTime = System.nanoTime();
        long watchDurationMs = NANOSECONDS.toMillis(gcStartNanoTime - watchStartNanoTime);

        out.println("LeakCanary 第一次主动去引用队列中获取弱引用");
        removeWeaklyReachableReferences();

        if (gone(reference)) {
            out.println("对象被回收了，回家吧");
            return DONE;
        }

        gcTrigger.runGc();
        out.println("LeakCanary run gc 后 第二次主动去引用队列中获取弱引用");
        removeWeaklyReachableReferences();


        if (!gone(reference)) {
            long startDumpHeap = System.nanoTime();
            long gcDurationMs = NANOSECONDS.toMillis(startDumpHeap - gcStartNanoTime);
            // 如果还没有消失, dump 内存了哈,
            out.println("LeakCanary 还没有消失, dump 内存了哈");

            out.println("LeakCanary 当前进程把堆内存写入文件");
            File heapDumpFile = heapDumper.dumpHeap();

            long heapDumpDurationMs = NANOSECONDS.toMillis(System.nanoTime() - startDumpHeap);
            out.println("LeakCanary 文件已经写完,耗时哦子线程工作 spend time ms:"
                    + heapDumpDurationMs + " ,thread name" + Thread.currentThread().getName());

            out.println("LeakCanary 把dump file 转化成对象 ...");
            long startFile2HeapDump = System.nanoTime();
            HeapDump heapDump = heapDumpBuilder.heapDumpFile(heapDumpFile).referenceKey(reference.key)
                    .referenceName(reference.name)
                    .watchDurationMs(watchDurationMs)
                    .gcDurationMs(gcDurationMs)
                    .heapDumpDurationMs(heapDumpDurationMs)
                    .build();

            out.println("LeakCanary 转化完毕耗时ms :"+NANOSECONDS.toMillis(System.nanoTime() - startFile2HeapDump));

            // 执行分析 (开启另外的进程 fork(内存copy)/clone(内存共享) )

        }
        return DONE;
    }

    private void removeWeaklyReachableReferences() {
        KeyedWeakReference ref = null;
        // 弱引用关联的对象被回收，弱引用会进入引用队列
        while ((ref = (KeyedWeakReference)queue.poll()) != null) {
            retainedKeys.remove(ref.key);
        }
    }

    private boolean gone(KeyedWeakReference reference) {
        return !retainedKeys.contains(reference.key);
    }
}
