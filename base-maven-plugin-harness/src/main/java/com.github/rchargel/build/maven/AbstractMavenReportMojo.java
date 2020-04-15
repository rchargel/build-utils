package com.github.rchargel.build.maven;

import com.github.rchargel.build.report.Messages;
import com.github.rchargel.build.report.Report;
import com.github.rchargel.build.report.Report.ReportBuilder;

import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class AbstractMavenReportMojo extends AbstractMavenReport {

    private Messages messages;

    @Override
    protected void executeReport(final Locale locale) throws MavenReportException {
        final ReportBuilder builder = executeReportMojo(getMessages(locale))
                .projectVersion(project.getVersion())
                .publishDate(LocalDate.now());

        generateFinalReport(builder.build());
    }

    protected abstract ReportBuilder executeReportMojo(Messages messages) throws MavenReportException;

    protected void generateFinalReport(final Report report) throws MavenReportException {
        new ReportSiteGenerator(getSink()).generateReport(report);
    }

    protected Messages getMessages(final Locale locale) {
        if (messages == null) {
            messages = new Messages(ResourceBundle.getBundle(getBundleName(), locale));
        }
        return messages;
    }

    protected String getBundleName() {
        return "messages";
    }

    @Override
    public String getName(final Locale locale) {
        return getMessages(locale).text("report.title");
    }

    @Override
    public String getDescription(final Locale locale) {
        return getMessages(locale).text("report.description");
    }

    protected void cleanFile(final File file) throws MavenReportException {
        if (file.exists() && !file.delete())
            throw new MavenReportException("Could not clean file: " + file.getAbsolutePath());

        final File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs())
            throw new MavenReportException("Could not create directory: " + file.getAbsolutePath());
        try {
            if (!file.createNewFile())
                throw new MavenReportException("Could not create file: " + file.getAbsolutePath());
        } catch (final IOException e) {
            throw new MavenReportException(e.getMessage(), e);
        }
    }

//    @Override
//    public boolean isExternalReport() {
//        return true;
//    }
}
