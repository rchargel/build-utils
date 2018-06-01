package net.zcarioca.maven.benchmark;

import static net.dempsy.util.Functional.recheck;
import static net.dempsy.util.Functional.uncheck;
import static net.zcarioca.maven.AbstractMavenMojo.getResourceAsFile;
import static net.zcarioca.maven.SearchUtil.findClassesContainingAnnotation;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.CompilerHints;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import net.zcarioca.maven.benchmark.results.BenchmarkResults;
import net.zcarioca.maven.benchmark.results.BenchmarkTestResult;

public class BenchmarkExecutor {

    static {
        initCompilerHints();
    }

    private final Log log;

    public BenchmarkExecutor(final Log log) {
        this.log = log;
    }

    public BenchmarkResults executeBenchmarks() throws MojoExecutionException {
        return new BenchmarkResults(recheck(() -> findClassesContainingAnnotation(Benchmark.class)
                .map(this::createOptions)
                .map(this::createRunner)
                .map(r -> uncheck(() -> executeRunner(r)))
                .map(BenchmarkTestResult::build)
                .flatMap(List::stream)
                .reduce(null, BenchmarkExecutor::addResultToSet, BenchmarkExecutor::merge)
                .values(), MojoExecutionException.class));
    }

    private Options createOptions(final Class<?> benchmarkClass) {
        log.info("Benchmark: " + benchmarkClass.getSimpleName());
        return new OptionsBuilder().forks(1).include(benchmarkClass.getCanonicalName()).build();
    }

    private Collection<RunResult> executeRunner(final Runner runner) throws MojoExecutionException {
        try {
            return runner.run();
        } catch (final RunnerException e) {
            log.error(e.getMessage(), e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
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
}
