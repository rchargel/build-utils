package net.zcarioca.build.common;

import com.fake.classes.classes.FakeAnnotation;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertArrayEquals;

public class CanSearchClasspathTest {
    @BeforeClass
    public static void setup() {
        final ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{
                ClasspathUtil.class.getProtectionDomain().getCodeSource().getLocation(),
                CanSearchClasspathTest.class.getProtectionDomain().getCodeSource().getLocation()
        }, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Test
    public void testMethodAnnotation() {
        final String[] methods = ClasspathUtil.findMethodsAnnotatedWith(FakeAnnotation.class)
                .map(Method::getName)
                .toArray(String[]::new);
        assertArrayEquals(new String[]{"add"}, methods);
    }

    @Test
    public void testClassWithMethodAnnotation() {
        final String[] methods = ClasspathUtil.findClassesWithMethodAnnotation(FakeAnnotation.class)
                .map(Class::getSimpleName)
                .toArray(String[]::new);
        assertArrayEquals(new String[]{"ClassWithMethodAnnotation"}, methods);
    }

    @Test
    public void testClassAnnotation() {
        final String[] methods = ClasspathUtil.findClassesAnnotatedWith(FakeAnnotation.class)
                .map(Class::getSimpleName)
                .toArray(String[]::new);
        assertArrayEquals(new String[]{"ClassWithAnnotation"}, methods);
    }

    @Test
    public void testAnyAnnotation() {
        final String[] methods = ClasspathUtil.findClassesContainingAnnotation(FakeAnnotation.class)
                .map(Class::getSimpleName)
                .sorted()
                .toArray(String[]::new);
        assertArrayEquals(new String[]{"ClassWithAnnotation", "ClassWithMethodAnnotation"}, methods);
    }
}
