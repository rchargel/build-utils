package com.github.rchargel.build.common;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.scanners.AbstractScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class ClasspathUtil {
    private ClasspathUtil() throws InstantiationException {
        throw new InstantiationException("This class cannot be instantiated");
    }

    /**
     * Reads a file out of the classpath.
     *
     * @param path The path to the file
     * @return Returns an InputStream
     */
    public static Reader readFromClasspath(final String path) {
        requireNonNull(path);
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
    }

    /**
     * Finds all classes which extend/implement the provided type.
     *
     * @param parentType The parent type
     * @param <T>        The "type" of the parent class
     * @return Returns a stream of classes
     */
    public static <T> Stream<Class<? extends T>> findSubTypes(final Class<T> parentType) {
        requireNonNull(parentType);
        return new Reflections(createConfigurationBuilder().addScanners(new SubTypesScanner())).getSubTypesOf(parentType).stream();
    }

    /**
     * Finds all of the classes within a given package.
     *
     * @param basePackage The package to search in
     * @return Returns a stream of classes
     */
    public static Stream<Class<?>> findClassesInPackage(final String basePackage) {
        requireNonNull(basePackage);

        return new Reflections(createConfigurationBuilder()
                .addScanners(new CustomTypesScanner())
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(basePackage))))
                .getStore()
                .getAll(CustomTypesScanner.class, Object.class.getCanonicalName())
                .stream()
                .map(s -> tryToLoadClass(s))
                .filter(Objects::nonNull)
                .distinct()
                .map(c -> (Class<?>) c);
    }

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
     * @param <T>        Any {@link Object} type.
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
    public static Stream<Class<?>> findClassesWithMethodAnnotation(final Class<? extends Annotation> annotation) {
        requireNonNull(annotation);
        return getMethodsAnnotatedInPackage(annotation).map(m -> m.getDeclaringClass());
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
                    .setUrls(loadClassURLs())
                    .addClassLoaders(ClassLoader.getSystemClassLoader(), Thread.currentThread().getContextClassLoader())
                    .addScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner()));
        }

    }

    @NotNull
    private static Set<URL> loadClassURLs() {
        final Set<URL> urls = Stream.concat(Stream.concat(ClasspathHelper.forClassLoader().stream(),
                ClasspathHelper.forClassLoader(ClassLoader.getSystemClassLoader(),
                        Thread.currentThread().getContextClassLoader()).stream()),
                Stream.of(ClasspathUtil.class.getProtectionDomain().getCodeSource().getLocation()))
                .collect(Collectors.toSet());

        urls.forEach(System.out::println);

        return urls;
    }

    private static Class<?> tryToLoadClass(final String className) {
        try {
            return Class.forName(className);
        } catch (final Exception e) {
            return null;
        }
    }

    private static ConfigurationBuilder createConfigurationBuilder() {
        return new ConfigurationBuilder()
                .setUrls(loadClassURLs())
                .addClassLoaders(ClassLoader.getSystemClassLoader(), Thread.currentThread().getContextClassLoader());
    }

    private static class CustomTypesScanner extends AbstractScanner {

        @Override
        public void scan(final Object o, final Store store) {
            final String className = getMetadataAdapter().getClassName(o);
            if (acceptResult(className))
                put(store, Object.class.getCanonicalName(), className);
        }
    }

}
