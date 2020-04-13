package com.github.rchargel.build.benchmark.maven.plugin;

import com.github.rchargel.build.benchmark.BenchmarkExecutor;
import com.github.rchargel.build.benchmark.results.BenchmarkResults;
import com.github.rchargel.build.maven.AbstractMavenMojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingMojo extends AbstractMavenMojo {

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/" + JMHConstants.OUTPUT_DIR_NAME)
    private String outputDirectory;

    @Override
    protected void executeMojo() throws MojoExecutionException {
        getLog().info("Running Benchmarks");

        final BenchmarkResults results;
        final File file = new File(outputDirectory, JMHConstants.JSON_FILE_NAME);
        cleanFile(file);
        try (final OutputStream outputStream = new FileOutputStream(file)) {
            results = new BenchmarkExecutor().executeBenchmarks();
            getLog().info("Writing JSON to " + file.getAbsolutePath());
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(outputStream, results);
        } catch (final Exception e) {
            getLog().error(e.getMessage(), e);
            throw new MojoExecutionException(e.getMessage(), e);
        }

//        final ResourceBundle bundle = getBundle("messages");
//
//        getLog().info("Generating HTML report");
//        return BenchmarkReport.buildReport(results, new Messages(bundle))
//                .projectVersion(project.getVersion())
//                .publishDate(LocalDate.now());
    }

}
