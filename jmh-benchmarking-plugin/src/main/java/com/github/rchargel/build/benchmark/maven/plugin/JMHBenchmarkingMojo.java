package com.github.rchargel.build.benchmark.maven.plugin;

import com.github.rchargel.build.benchmark.BenchmarkExecutor;
import com.github.rchargel.build.benchmark.report.BenchmarkReport;
import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.maven.AbstractMavenReportMojo;
import com.github.rchargel.build.report.Messages;
import com.github.rchargel.build.report.ReportBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ResourceBundle;

@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingMojo extends AbstractMavenReportMojo {

    private static final String JSON_FILE_NAME = "benchmark-reports.json";
    private static final String REPORT_FILE_NAME = "benchmark-reports.html";

    @Override
    protected ReportBuilder executeReportMojo() throws MojoExecutionException {
        getLog().info("Running Benchmarks");

        final BenchmarkResults results;
        final File file = new File(reportingDirectory, JSON_FILE_NAME);
        cleanFile(file);
        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), getOutputEncoding())) {
            results = new BenchmarkExecutor().executeBenchmarks();
            getLog().info("Writing JSON to " + file.getAbsolutePath());
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, results);
        } catch (final Exception e) {
            getLog().error(e.getMessage(), e);
            throw new MojoExecutionException(e.getMessage(), e);
        }

        final ResourceBundle bundle = getBundle("messages");

        getLog().info("Generating HTML report");
        return BenchmarkReport.buildReport(results, new Messages(bundle))
                .projectVersion(project.getVersion())
                .publishDate(LocalDate.now());
    }

    @Override
    protected String getOutputFileName() {
        return REPORT_FILE_NAME;
    }

}
