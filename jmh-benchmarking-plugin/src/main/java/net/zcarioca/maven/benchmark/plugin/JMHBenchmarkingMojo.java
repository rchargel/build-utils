package net.zcarioca.maven.benchmark.plugin;

import net.zcarioca.maven.AbstractMavenReportMojo;
import net.zcarioca.maven.benchmark.BenchmarkExecutor;
import net.zcarioca.maven.benchmark.reports.BenchmarkTestReport;
import net.zcarioca.maven.benchmark.results.BenchmarkResults;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingMojo extends AbstractMavenReportMojo {

    private static final String JSON_FILE_NAME = "benchmark-reports.json";
    private static final String REPORT_FILE_NAME = "benchmark-reports.html";
    private static final String GOAL_NAME = "run";

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {
        getLog().info("Running Benchmarks");

        final BenchmarkResults results;
        final File file = new File(outputDirectory, JSON_FILE_NAME);
        try {
            results = new BenchmarkExecutor().executeBenchmarks();
            FileUtils.write(file, results.toString(), getOutputEncoding());
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        generateFinalReport((sink, log, locale, bundle, encoding) -> new BenchmarkTestReport(results, sink, log, locale, bundle, encoding),
                REPORT_FILE_NAME);
    }

    @Override
    protected String getGoalName() {
        return GOAL_NAME;
    }
}
