package com.dodola.leakcanarydemo.leakcanary;

public interface WatchExecutor {
    WatchExecutor NONE = new WatchExecutor() {
        @Override public void execute(Retryable retryable) {
        }
    };
    void execute(Retryable retryable);
}
