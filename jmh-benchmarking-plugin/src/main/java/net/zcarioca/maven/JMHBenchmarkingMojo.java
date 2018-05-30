package net.zcarioca.maven;

import static net.dempsy.util.Functional.recheck;
import static net.dempsy.util.Functional.uncheck;
import static net.zcarioca.maven.SearchUtil.findClassesContainingAnnotation;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.CompilerHints;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingMojo extends AbstractMavenMojo {
    @Parameter(defaultValue = "${project.build.directory}/benchmark-reports")
    private File reportsDirectory;

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {
        getLog().info("Running Benchmark Tests");
        initCompilerHints();

        final File benchmarkFile = new File(testClassesDirectory, "META-INF/BenchmarkList");
        getLog().info(benchmarkFile.getAbsolutePath() + ": " + benchmarkFile.exists());
        final Map<String, BenchmarkTestResult> results;
        try {
            results = recheck(() -> findClassesContainingAnnotation(Benchmark.class)
                    .map(this::createOptions)
                    .map(this::createRunner)
                    .map(r -> uncheck(() -> r.run()))
                    .map(BenchmarkTestResult::build)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(r -> r.getKey(), r -> r, (r1, r2) -> r1.merge(r2))), RunnerException.class);
        } catch (final RunnerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        getLog().info(results.toString());
    }

    private Options createOptions(final Class<?> benchmarkClass) {
        getLog().info("Running test: " + benchmarkClass.getSimpleName());
        return new OptionsBuilder().forks(1).include(benchmarkClass.getCanonicalName()).build();
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

    static void initCompilerHints() {
        try {
            final String compilerHintsFile = getResourceAsFile("/META-INF/CompilerHints");
            final Field field = CompilerHints.class.getDeclaredField("defaultList");
            field.setAccessible(true);
            field.set(null, CompilerHints.fromFile(compilerHintsFile));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
