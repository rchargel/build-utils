package com.github.rchargel.build.common;

import com.github.rchargel.build.test.ClassLoaderHelper;

import com.fake.classes.classes.FakeAnnotation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CanSearchClasspathTest {
    @BeforeClass
    public static void setup() {
        ClassLoaderHelper.addClassToClassLoader(CanSearchClasspathTest.class);
    }

    @Test
    public void findAllClassesInPackage() {
        final Set<Class<?>> classes = ClasspathUtil.findClassesInPackage("com.fake")
                .collect(Collectors.toSet());

        assertEquals(3, classes.size());
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

    @Test
    public void cannotInstantiateClasspathUtil() throws Exception {
        final Constructor<?> c = ClasspathUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        final Exception e = Assert.assertThrows(InvocationTargetException.class, () -> c.newInstance());
        assertEquals(InstantiationException.class, e.getCause().getClass());
    }

    @Test
    public void findResource() throws Exception {
        final String path = ClasspathUtil.getResourceAsFile("com/fake/classes/classes/FakeAnnotation.class");
        assertTrue(path.endsWith("FakeAnnotation.class"));

        try (final Reader reader = ClasspathUtil.readFromClasspath("com/fake/classes/classes/FakeAnnotation.class")) {
            assertTrue(reader.read() > -1);
        }
    }

    @Test
    public void canSeeIfClassExists() throws Exception {
        assertTrue(ClasspathUtil.classExists("java.lang.String"));
        assertFalse(ClasspathUtil.classExists("java.lang.NonString"));
    }
}
