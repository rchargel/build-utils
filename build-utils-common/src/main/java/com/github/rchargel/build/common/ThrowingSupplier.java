package com.github.rchargel.build.common;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<R> {
    R get() throws Throwable;
}
