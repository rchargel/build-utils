package com.github.rchargel.build.maven;

import com.github.rchargel.build.report.ReportBuilder;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public abstract class AbstractMavenReportMojo extends AbstractMavenMojo {

    @Parameter(defaultValue = "${project.reporting.outputDirectory}", readonly = true, required = true)
    protected File outputDirectory;
    @Parameter(property = "outputEncoding", defaultValue = "${project.reporting.outputEncoding}", readonly = true)
    private String outputEncoding;
    @Parameter(defaultValue = "${user.language}")
    private String userLanguage;
    @Parameter(defaultValue = "${user.country}")
    private String userCountry;

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {
        final ReportBuilder reportBuilder = executeReportMojo();
        generateFinalReport(reportBuilder, getOutputFileName());
    }

    protected abstract ReportBuilder executeReportMojo() throws MojoExecutionException, MojoFailureException;

    protected void generateFinalReport(final ReportBuilder reportBuilder, final String outputFileName) throws MojoExecutionException {
        final File outputFile = new File(outputDirectory, outputFileName);
        try (final Writer writer = new BufferedWriter(new FileWriterWithEncoding(outputFile.getAbsolutePath(), getOutputEncoding(), false))) {
            reportBuilder.writeReportTo(writer);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract String getOutputFileName();

    protected Charset getOutputEncoding() {
        return Optional.ofNullable(outputEncoding)
                .filter(StringUtils::isNotBlank)
                .map(Charset::forName)
                .orElse(StandardCharsets.UTF_8);
    }

    protected ResourceBundle getBundle(final String bundleName) {
        final Locale locale = Optional.ofNullable(userLanguage)
                .filter(StringUtils::isNotBlank)
                .map(language -> Optional.ofNullable(userCountry)
                        .filter(StringUtils::isNotBlank)
                        .map(country -> new Locale(language, country))
                        .orElseGet(() -> new Locale(language)))
                .orElseGet(Locale::getDefault);
        return ResourceBundle.getBundle(bundleName, locale);
    }

}
