package net.zcarioca.maven;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.logging.Log;

@FunctionalInterface
public interface ReportRendererSupplier<T extends AbstractSystemReportRenderer> {

    public T create(Sink sink, Log log, Locale local, ResourceBundle bundle, String encoding);
}
