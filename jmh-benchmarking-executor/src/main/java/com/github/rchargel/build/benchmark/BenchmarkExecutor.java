package com.github.rchargel.build.benchmark;

import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.benchmark.results.BenchmarkTestResult;

import org.openjdk.jmh.annotations.Benchmark;
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
import static com.github.rchargel.build.common.RuntimeUtils.getOptimizedThreads;

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

    public BenchmarkResults executeBenchmarks(final double minAllowedPValue, final int numberOfTestRepetitions) throws RunnerException {

        final Collection<BenchmarkTestResult> results = recheck(() -> findClassesContainingAnnotation(Benchmark.class)
                .map(this::createOptions)
                .map(this::createRunner)
                .flatMap(r -> IntStream.range(0, numberOfTestRepetitions).mapToObj(i -> uncheck(() -> executeRunner(r))))
                .flatMap(r -> r.stream().map(BenchmarkTestResult::fromRunResult))
                .reduce(null, BenchmarkExecutor::addResultToSet, BenchmarkExecutor::merge)
                .values(), RunnerException.class);

        return BenchmarkResults.buildFromResults(results, minAllowedPValue);
    }

    private Collection<RunResult> executeRunner(final Runner runner) throws RunnerException {
        return runner.run();
    }

    private Options createOptions(final Class<?> benchmarkClass) {
        return new OptionsBuilder()
                .forks(getOptimizedThreads())
                .threads(1)
                .include(benchmarkClass.getCanonicalName())
                .build();
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
