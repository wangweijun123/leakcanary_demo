package com.dodola.leakcanarydemo.leakcanary;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class KeyedWeakReference extends WeakReference<Object> {
    public String key;
    public final String name;

    public KeyedWeakReference(String key, String name, Object referent, ReferenceQueue<? super Object> q) {
        super(referent, q);
        this.key = key;
        this.name = name;
    }
}
