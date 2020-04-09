package com.github.rchargel.build.common;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class ClasspathUtil {
    /**
     * Finds all of the classes annotated with the given annotation.
     *
     * @param annotation The annotation to scan for.
     * @return Returns a stream of methods.
     */
    public static Stream<Class<?>> findClassesAnnotatedWith(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);

        return getClassesAnnotatedInPackage(annotation);
    }

    private static Stream<Class<?>> getClassesAnnotatedInPackage(final Class<? extends Annotation> annotation) {
        return Optional.ofNullable(LazyReflections.reflections.getTypesAnnotatedWith(annotation)).orElse(new HashSet<>()).stream();
    }

    /**
     * Finds all of the classes which have either been annotated with, or have a method annotated with, the given annotation.
     *
     * @param annotation The annotation to scan for.
     * @return Returns a stream of methods.
     */
    public static <T extends Object> Stream<Class<?>> findClassesContainingAnnotation(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);

        return Stream.concat(getClassesAnnotatedInPackage(annotation),
                getMethodsAnnotatedInPackage(annotation)
                        .map(m -> (Class<T>) m.getDeclaringClass()));
    }

    private static Stream<Method> getMethodsAnnotatedInPackage(final Class<? extends Annotation> annotation) {
        return Optional.ofNullable(LazyReflections.reflections.getMethodsAnnotatedWith(annotation)).orElse(new HashSet<>()).stream();
    }

    /**
     * Finds all of the classes where there is a method with the given annotation.
     *
     * @param annotation The annotation to scan for.
     * @return Returns a stream of methods.
     */
    public static <T extends Object> Stream<Class<?>> findClassesWithMethodAnnotation(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);
        return getMethodsAnnotatedInPackage(annotation).map(m -> (Class<T>) m.getDeclaringClass());
    }

    /**
     * Finds all of the methods in a given package tree annotated with the given class.
     *
     * @param annotation The annotation to scan for.
     * @return Returns a stream of methods.
     */
    public static Stream<Method> findMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);

        return getMethodsAnnotatedInPackage(annotation);
    }

    /**
     * Gets a classpath resource as an absolute file path.
     *
     * @param resourceName The name of the resource.
     * @return Returns a complete filepath
     * @throws Exception thrown if the resource cannot be found.
     */
    public static String getResourceAsFile(final String resourceName) throws Exception {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final String findName = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;

        final Enumeration<URL> urls = loader.getResources(findName);
        while (urls.hasMoreElements()) {
            final URI url = urls.nextElement().toURI();
            if (!url.getScheme().toLowerCase().startsWith("jar")) {
                final File file = new File(url);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        throw new RuntimeException("Unable to find resource " + resourceName);
    }

    private static class LazyReflections {
        public static final Reflections reflections;

        static {
            reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(Stream.concat(ClasspathHelper.forClassLoader().stream(),
                            ClasspathHelper.forClassLoader(ClassLoader.getSystemClassLoader(),
                                    Thread.currentThread().getContextClassLoader()).stream())
                            .collect(Collectors.toSet()))
                    .addClassLoaders(ClassLoader.getSystemClassLoader(), Thread.currentThread().getContextClassLoader())
                    .addScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner()));
        }

    }
}
