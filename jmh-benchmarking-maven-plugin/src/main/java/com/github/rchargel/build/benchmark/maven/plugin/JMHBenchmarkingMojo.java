package com.github.rchargel.build.benchmark.maven.plugin;

import com.github.rchargel.build.benchmark.BenchmarkExecutor;
import com.github.rchargel.build.benchmark.report.BenchmarkReport;
import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.maven.AbstractMavenMojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingMojo extends AbstractMavenMojo {

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/" + JMHConstants.OUTPUT_DIR_NAME)
    private String outputDirectory;

    @Parameter(name = "baselineRun", required = false)
    private String baselineRun;

    @Parameter(name = "maxAbsZScore", defaultValue = "1.5")
    private double maxAbsZScore;

    @Parameter(name = "ignoreHardwareChanges", defaultValue = "false")
    private boolean ignoreHardwareChanges;

    @Parameter(name = "numberOfTestRepetitions", defaultValue = "1")
    private int numberOfTestRepetitions;

    @Parameter(name = "failBuildOnErrors", defaultValue = "false")
    private boolean failBuildOnErrors;

    private ObjectMapper mapper;

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {
        getLog().info("Running Benchmarks");

        final BenchmarkResults results;
        final File file = new File(outputDirectory, JMHConstants.JSON_FILE_NAME);
        cleanFile(file);
        try (final OutputStream outputStream = new FileOutputStream(file)) {
            results = compareToBaseline(new BenchmarkExecutor().executeBenchmarks(maxAbsZScore, numberOfTestRepetitions));
            getLog().info("Writing JSON to " + file.getAbsolutePath());
            objectMapper().writerWithDefaultPrettyPrinter().writeValue(outputStream, results);
        } catch (final Exception e) {
            getLog().error(e.getMessage(), e);
            throw new MojoExecutionException(e.getMessage(), e);
        }

        final File htmlFile = new File(outputDirectory, JMHConstants.HTML_FILE_NAME);
        cleanFile(htmlFile);
        getLog().info("Writing HTML to " + htmlFile.getAbsolutePath());

        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(htmlFile), StandardCharsets.UTF_8)) {
            BenchmarkReport.buildReport(results, getMessages(getLocale()))
                    .projectVersion(project.getVersion())
                    .publishDate(LocalDate.now())
                    .build()
                    .writeReportTo(writer);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (failBuildOnErrors && results.getFailsMaxAbsZScore()) {
            throw new MojoFailureException("Some evaluations did not meet the minimum p-value requirement");
        }
    }

    private ObjectMapper objectMapper() {
        if (mapper == null)
            mapper = new ObjectMapper();
        return mapper;
    }

    private BenchmarkResults compareToBaseline(final BenchmarkResults results) throws IOException {
        if (baselineRun != null) {
            getLog().info("Looking for baseline run: " + baselineRun);
            final File baselineRunFile = new File(baselineRun);
            if (baselineRunFile.isFile()) {
                try (final BufferedReader reader = new BufferedReader(new FileReader(baselineRunFile))) {
                    getLog().info("Comparing to baseline results: " + baselineRunFile.getAbsolutePath());
                    final BenchmarkResults baselineResults = objectMapper().readValue(reader, BenchmarkResults.class);
                    return results.compareToBaseline(baselineResults, ignoreHardwareChanges);
                }
            }
        }
        return results;
    }

}
