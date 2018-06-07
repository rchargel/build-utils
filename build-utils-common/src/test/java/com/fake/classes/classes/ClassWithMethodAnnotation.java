package com.fake.classes.classes;

public class ClassWithMethodAnnotation {

    @FakeAnnotation
    public int add(final int a, final int b) {
        return a + b;
    }
}
