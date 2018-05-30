package net.zcarioca.maven;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class SearchUtil {

    private static class LazyReflections {
        public static final Reflections reflections;
        static {
            reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(Stream.concat(ClasspathHelper.forClassLoader().stream(), ClasspathHelper
                            .forClassLoader(ClassLoader.getSystemClassLoader(), Thread.currentThread().getContextClassLoader()).stream())
                            .collect(Collectors.toSet()))
                    .addClassLoader(ClassLoader.getSystemClassLoader())
                    .addClassLoader(Thread.currentThread().getContextClassLoader())
                    .addScanners(new TypeAnnotationsScanner(),
                            new MethodAnnotationsScanner()));
        }

    }

    /**
     * Finds all of the classes annotated with the given annotation.
     *
     * @param annotation
     *            The annotation to scan for.
     * @return Returns a stream of methods.
     */
    public static Stream<Class<?>> findClassesAnnotatedWith(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);

        return getClassesAnnotatedInPackage(annotation);
    }

    /**
     * Finds all of the classes which have either been annotated with, or have a method annotated with, the given annotation.
     *
     * @param annotation
     *            The annotation to scan for.
     * @return Returns a stream of methods.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> Stream<Class<?>> findClassesContainingAnnotation(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);

        return Stream.concat(getClassesAnnotatedInPackage(annotation),
                getMethodsAnnotatedInPackage(annotation)
                        .map(m -> (Class<T>) m.getDeclaringClass()));
    }

    /**
     * Finds all of the classes where there is a method with the given annotation.
     *
     * @param annotation
     *            The annotation to scan for.
     * @return Returns a stream of methods.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> Stream<Class<?>> findClassesWithMethodAnnotation(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);
        return getMethodsAnnotatedInPackage(annotation)
                .map(m -> (Class<T>) m.getDeclaringClass());
    }

    /**
     * Finds all of the methods in a given package tree annotated with the given class.
     *
     * @param annotation
     *            The annotation to scan for.
     * @return Returns a stream of methods.
     */
    public static Stream<Method> findMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);

        return getMethodsAnnotatedInPackage(annotation);
    }

    private static Stream<Class<?>> getClassesAnnotatedInPackage(final Class<? extends Annotation> annotation) {
        return Optional.ofNullable(LazyReflections.reflections.getTypesAnnotatedWith(annotation)).orElse(new HashSet<>()).stream();
    }

    private static Stream<Method> getMethodsAnnotatedInPackage(final Class<? extends Annotation> annotation) {
        return Optional.ofNullable(LazyReflections.reflections.getMethodsAnnotatedWith(annotation)).orElse(new HashSet<>()).stream();
    }

}
