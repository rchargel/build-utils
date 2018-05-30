package net.zcarioca.maven;

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fake.classes.FakeAnnotation;

public class SearchUtilTest {
    @BeforeClass
    public static void setup() {
        final ClassLoader classLoader = URLClassLoader.newInstance(new URL[] {
                SearchUtil.class.getProtectionDomain().getCodeSource().getLocation(),
                SearchUtilTest.class.getProtectionDomain().getCodeSource().getLocation()
        }, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Test
    public void testMethodAnnotation() {
        final String[] methods = SearchUtil.findMethodsAnnotatedWith(FakeAnnotation.class)
                .map(Method::getName)
                .toArray(String[]::new);
        assertArrayEquals(new String[] { "add" }, methods);
    }

    @Test
    public void testClassWithMethodAnnotation() {
        final String[] methods = SearchUtil.findClassesWithMethodAnnotation(FakeAnnotation.class)
                .map(Class::getSimpleName)
                .toArray(String[]::new);
        assertArrayEquals(new String[] { "ClassWithMethodAnnotation" }, methods);
    }

    @Test
    public void testClassAnnotation() {
        final String[] methods = SearchUtil.findClassesAnnotatedWith(FakeAnnotation.class)
                .map(Class::getSimpleName)
                .toArray(String[]::new);
        assertArrayEquals(new String[] { "ClassWithAnnotation" }, methods);
    }

    @Test
    public void testAnyAnnotation() {
        final String[] methods = SearchUtil.findClassesContainingAnnotation(FakeAnnotation.class)
                .map(Class::getSimpleName)
                .sorted()
                .toArray(String[]::new);
        assertArrayEquals(new String[] { "ClassWithAnnotation", "ClassWithMethodAnnotation" }, methods);
    }
}
