package com.github.rchargel.build.benchmark;

import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.benchmark.results.BenchmarkTestResult;
import com.github.rchargel.build.common.RuntimeUtils;

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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.rchargel.build.common.ClasspathUtil.findClassesContainingAnnotation;
import static com.github.rchargel.build.common.ClasspathUtil.getResourceAsFile;

import static net.dempsy.util.Functional.recheck;
import static net.dempsy.util.Functional.uncheck;

public class BenchmarkExecutor {

    static {
        initCompilerHints();
    }

    private static void initCompilerHints() {
        try {
            final String compilerHintsFile = getResourceAsFile("/META-INF/CompilerHints");
            final Field field = CompilerHints.class.getDeclaredField("defaultList");
            field.setAccessible(true);
            field.set(null, CompilerHints.fromFile(compilerHintsFile));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Map<String, BenchmarkTestResult> merge(final Map<String, BenchmarkTestResult> mapA, final Map<String, BenchmarkTestResult> mapB) {
        return Stream.of(mapA.entrySet(), mapB.entrySet())
                .flatMap(s -> s.stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v1.merge(v2)));
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

    BenchmarkResults executeBenchmarks(final double minAllowedPValue, final int numberOfTestRepetitions, final Stream<Class<?>> classesToTest) throws RunnerException {
        final Map<String, BenchmarkTestResult> testResultMap = recheck(() -> classesToTest
                .map(this::createOptions)
                .map(this::createRunner)
                .flatMap(r -> IntStream.range(0, numberOfTestRepetitions).mapToObj(i -> uncheck(() -> executeRunner(r))))
                .flatMap(r -> r.stream().map(BenchmarkTestResult::fromRunResult))
                .reduce(null, BenchmarkExecutor::addResultToSet, BenchmarkExecutor::merge), RunnerException.class);

        if (testResultMap == null || testResultMap.isEmpty())
            throw new RunnerException("No test results were produced");

        return BenchmarkResults.buildFromResults(testResultMap.values(), minAllowedPValue);
    }

    public BenchmarkResults executeBenchmarks(final double minAllowedPValue, final int numberOfTestRepetitions) throws RunnerException {
        final Stream<Class<?>> annotatedClasses = findClassesContainingAnnotation(Benchmark.class);
        return executeBenchmarks(minAllowedPValue, numberOfTestRepetitions, annotatedClasses);
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
                .jvmArgs("-server", "-disablesystemassertions")
                .build();
    }

    private int getForks(final Class<?> benchmarkClass) {
        final Fork fork = benchmarkClass.getAnnotation(Fork.class);
        if (fork != null)
            return Math.max(1, fork.value());
        return 1;
    }

    private int getWarmupForks(final Class<?> benchmarkClass) {
        final Fork fork = benchmarkClass.getAnnotation(Fork.class);
        if (fork != null)
            return Math.max(0, fork.warmups());
        return 0;
    }

    private int getThreads(final Class<?> benchmarkClass) {
        final Threads threads = benchmarkClass.getAnnotation(Threads.class);
        if (threads != null)
            return threads.value();
        return 1;
    }

    private Runner createRunner(final Options opts) {
        final Runner runner = new Runner(opts);
        try {
            final String benchmarkListFile = getResourceAsFile("/META-INF/BenchmarkList");

            final Field field = Runner.class.getDeclaredField("list");
            field.setAccessible(true);
            field.set(runner, BenchmarkList.fromFile(benchmarkListFile));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return runner;
    }
}
