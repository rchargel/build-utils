package net.zcarioca.maven.benchmark.plugin;

import java.io.File;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.zcarioca.maven.AbstractMavenMojo;
import net.zcarioca.maven.benchmark.BenchmarkExecutor;
import net.zcarioca.maven.benchmark.results.BenchmarkTestResult;

@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class JMHBenchmarkingMojo extends AbstractMavenMojo {
    @Parameter(defaultValue = "${project.build.directory}/benchmark-reports")
    private File reportsDirectory;

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {
        getLog().info("Running Benchmarks");

        final Collection<BenchmarkTestResult> results = new BenchmarkExecutor(getLog()).executeBenchmarks();
        getLog().info(results.toString());
    }
}
