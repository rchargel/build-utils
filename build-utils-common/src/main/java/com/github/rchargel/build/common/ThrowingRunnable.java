package com.github.rchargel.build.common;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Throwable;
}
