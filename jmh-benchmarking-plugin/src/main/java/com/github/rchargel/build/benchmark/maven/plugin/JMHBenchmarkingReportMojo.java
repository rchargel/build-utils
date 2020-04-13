package com.github.rchargel.build.benchmark.maven.plugin;

import com.github.rchargel.build.benchmark.report.BenchmarkReport;
import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.maven.AbstractMavenReportMojo;
import com.github.rchargel.build.report.Messages;
import com.github.rchargel.build.report.ReportBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Mojo(name = "report", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingReportMojo extends AbstractMavenReportMojo {

    public static final String RUN_GOAL_EXC = "Unable to read benchmark results file. Make certain that the 'run' goal was executed";
    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/" + JMHConstants.OUTPUT_DIR_NAME)
    private String outputDirectory;

    @Override
    protected ReportBuilder executeReportMojo(final Messages messages) throws MavenReportException {
        getLog().info("Generating Benchmark Report");
        final BenchmarkResults results = loadResults();
        return BenchmarkReport.buildReport(results, messages);
    }

    private BenchmarkResults loadResults() throws MavenReportException {
        final File resultsFile = new File(outputDirectory, JMHConstants.JSON_FILE_NAME);
        if (!resultsFile.exists()) {
            getLog().error(RUN_GOAL_EXC);
            throw new MavenReportException(RUN_GOAL_EXC);
        }

        try (final InputStream inputStream = new FileInputStream(resultsFile)) {
            return new ObjectMapper().readValue(inputStream, BenchmarkResults.class);
        } catch (final IOException e) {
            getLog().error(RUN_GOAL_EXC);
            throw new MavenReportException(RUN_GOAL_EXC, e);
        }
    }

    @Override
    public String getOutputName() {
        return JMHConstants.OUTPUT_DIR_NAME;
    }

}
