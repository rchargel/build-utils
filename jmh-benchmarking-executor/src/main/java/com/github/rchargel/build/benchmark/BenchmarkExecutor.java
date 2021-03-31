package com.github.rchargel.build.benchmark;

import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.benchmark.results.BenchmarkTestResult;

import org.apache.commons.collections4.MapUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.CompilerHints;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.rchargel.build.common.ClasspathUtil.findClassesContainingAnnotation;
import static com.github.rchargel.build.common.ClasspathUtil.getResourceAsFile;
import static com.github.rchargel.build.common.ExceptionWrapper.wrap;

import static net.dempsy.util.Functional.recheck;
import static net.dempsy.util.Functional.uncheck;

public class BenchmarkExecutor {

    static {
        initCompilerHints();
    }

    private static void initCompilerHints() {
        wrap(RuntimeException.class, () -> {
            final String compilerHintsFile = getResourceAsFile("/META-INF/CompilerHints");
            final Field field = CompilerHints.class.getDeclaredField("defaultList");
            field.setAccessible(true);
            field.set(null, CompilerHints.fromFile(compilerHintsFile));
        });
    }

    private static Map<String, BenchmarkTestResult> merge(final Map<String, BenchmarkTestResult> mapA, final Map<String, BenchmarkTestResult> mapB) {
        return Stream.of(mapA.entrySet(), mapB.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, BenchmarkTestResult::merge));
    }

    private static Map<String, BenchmarkTestResult> addResultToSet(final Map<String, BenchmarkTestResult> map, final BenchmarkTestResult result) {
        final Map<String, BenchmarkTestResult> resultMap = map == null ? new HashMap<>() : map;

        if (resultMap.containsKey(result.getKey())) {
            final BenchmarkTestResult original = resultMap.remove(result.getKey());
            resultMap.put(result.getKey(), original.merge(result));
        } else {
            resultMap.put(result.getKey(), result);
        }
        return resultMap;
    }

    BenchmarkResults executeBenchmarks(final double maxAbsZScore, final int numberOfTestRepetitions, final Stream<Class<?>> classesToTest) throws RunnerException {
        final Map<String, BenchmarkTestResult> testResultMap = recheck(() -> classesToTest
                .map(this::createOptions)
                .map(this::createRunner)
                .flatMap(r -> IntStream.range(0, numberOfTestRepetitions).mapToObj(i -> uncheck(() -> executeRunner(r))))
                .flatMap(r -> r.stream().map(BenchmarkTestResult::fromRunResult))
                .reduce(null, BenchmarkExecutor::addResultToSet, BenchmarkExecutor::merge), RunnerException.class);

        if (MapUtils.isEmpty(testResultMap))
            throw new RunnerException("No test results were produced");

        return BenchmarkResults.buildFromResults(testResultMap.values(), maxAbsZScore);
    }

    public BenchmarkResults executeBenchmarks(final double maxAbsZScore, final int numberOfTestRepetitions) throws RunnerException {
        final Stream<Class<?>> annotatedClasses = findClassesContainingAnnotation(Benchmark.class);
        return executeBenchmarks(maxAbsZScore, numberOfTestRepetitions, annotatedClasses);
    }

    private Collection<RunResult> executeRunner(final Runner runner) throws RunnerException {
        return runner.run();
    }

    private Options createOptions(final Class<?> benchmarkClass) {
        return new OptionsBuilder()
                .forks(getForks(benchmarkClass))
                .warmupForks(getWarmupForks(benchmarkClass))
                .threads(getThreads(benchmarkClass))
                .include(benchmarkClass.getCanonicalName())
                .jvmArgs("-server", "-disablesystemassertions", "-XX:-TieredCompilation")
                .build();
    }

    private <T extends Annotation> int getAnnotationValue(final Class<?> benchmarkClass, final Class<T> type, final Function<T, Integer> transform, final int min) {
        return Math.max(min, Optional.ofNullable(benchmarkClass.getAnnotation(type))
                .map(transform::apply)
                .orElse(min));
    }

    private int getForks(final Class<?> benchmarkClass) {
        return getAnnotationValue(benchmarkClass, Fork.class, Fork::value, 1);
    }

    private int getWarmupForks(final Class<?> benchmarkClass) {
        return getAnnotationValue(benchmarkClass, Fork.class, Fork::warmups, 0);
    }

    private int getThreads(final Class<?> benchmarkClass) {
        return getAnnotationValue(benchmarkClass, Threads.class, Threads::value, 1);
    }

    private Runner createRunner(final Options opts) {
        final Runner runner = new Runner(opts);
        wrap(RuntimeException.class, () -> {
            final String benchmarkListFile = getResourceAsFile("/META-INF/BenchmarkList");

            final Field field = Runner.class.getDeclaredField("list");
            field.setAccessible(true);
            field.set(runner, BenchmarkList.fromFile(benchmarkListFile));
        });
        return runner;
    }
}
