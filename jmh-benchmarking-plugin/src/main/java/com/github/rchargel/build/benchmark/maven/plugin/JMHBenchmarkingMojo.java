package com.github.rchargel.build.benchmark.maven.plugin;

import com.github.rchargel.build.benchmark.BenchmarkExecutor;
import com.github.rchargel.build.benchmark.maven.reports.BenchmarkTestReport;
import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.maven.AbstractMavenReportMojo;
import com.github.rchargel.build.report.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.util.function.BiFunction;

@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingMojo extends AbstractMavenReportMojo {

    private static final String JSON_FILE_NAME = "benchmark-reports.json";
    private static final String REPORT_FILE_NAME = "benchmark-reports.html";
    private static final String GOAL_NAME = "run";

    @Override
    protected BiFunction<Sink, ReportBuilder<BenchmarkTestReport>, BenchmarkTestReport> executeReportMojo() throws MojoExecutionException {
        getLog().info("Running Benchmarks");

        final BenchmarkResults results;
        final File file = new File(outputDirectory, JSON_FILE_NAME);
        try {
            results = new BenchmarkExecutor().executeBenchmarks();
            FileUtils.write(file, results.toString(), getOutputEncoding());
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        return (sink, builder) -> new BenchmarkTestReport(results, sink, builder);
    }

    @Override
    protected String getOutputFileName() {
        return REPORT_FILE_NAME;
    }

    @Override
    protected String getGoalName() {
        return GOAL_NAME;
    }
}
