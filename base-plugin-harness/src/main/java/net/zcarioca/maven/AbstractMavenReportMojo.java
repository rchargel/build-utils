package net.zcarioca.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.ReaderFactory;

public abstract class AbstractMavenReportMojo extends AbstractMavenMojo {

    private static final String DEFAULT_COUNTRY = "US";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DOXIA_TEMPLATE = "org/apache/maven/doxia/siterenderer/resources/default-site.vm";

    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", readonly = true)
    private String inputEncoding;

    @Parameter(property = "outputEncoding", defaultValue = "${project.reporting.outputEncoding}", readonly = true)
    private String outputEncoding;

    @Parameter(defaultValue = "${project.reporting.outputDirectory}", readonly = true, required = true)
    protected File outputDirectory;

    @Parameter(defaultValue = "${user.language}")
    private String userLanguage;

    @Parameter(defaultValue = "${user.country}")
    private String userCountry;

    @Component
    private Renderer renderer;

    protected String getInputEncoding() {
        return inputEncoding == null ? ReaderFactory.ISO_8859_1 : inputEncoding;
    }

    protected String getOutputEncoding() {
        return outputEncoding == null ? ReaderFactory.UTF_8 : outputEncoding;
    }

    protected Locale getLocale() {
        final String language = StringUtils.defaultString(userLanguage, DEFAULT_LANGUAGE);
        final String country = StringUtils.defaultString(userCountry, DEFAULT_COUNTRY);
        final Locale locale = new Locale(language, country);
        return locale;
    }

    private final String getGeneratorName() throws MojoExecutionException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("generator.txt")) {
            return IOUtils.toString(inputStream, getInputEncoding()) + getGoalName();
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract String getGoalName();

    protected <R extends AbstractSystemReportRenderer> void generateFinalReport(final ReportRendererSupplier<R> rendererSupplier,
            final String outputFileName) throws MojoExecutionException, MojoFailureException {
        if (!outputDirectory.exists()) {
            if (!outputDirectory.isDirectory()) {
                throw new MojoExecutionException(outputDirectory.getAbsolutePath() + " is not a directory");
            }
            if (!outputDirectory.mkdirs()) {
                throw new MojoExecutionException("Could not create output directory: " + outputDirectory.getAbsolutePath());
            }
        }

        final String filename = outputFileName;
        final Locale locale = Locale.getDefault();

        final SiteRenderingContext siteContext = new SiteRenderingContext();
        siteContext.setDecoration(new DecorationModel());
        siteContext.setTemplateName(DOXIA_TEMPLATE);
        siteContext.setLocale(locale);
        siteContext.setTemplateProperties(getTemplateProperties());

        final RenderingContext context = new RenderingContext(outputDirectory, filename, getGeneratorName());
        getLog().info("Generating report to " + context.getRelativePath());

        final SiteRendererSink sink = new CustomSiteSink(context);

        rendererSupplier.create(sink, getLog(), getLocale(), getBundle(), getOutputEncoding()).render();

        sink.close();

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(outputDirectory, filename)), getOutputEncoding())) {
            renderer.mergeDocumentIntoSite(writer, sink, siteContext);
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle("messages", getLocale());
    }

    private Map<String, ?> getTemplateProperties() {
        final Map<String, Object> templateProperties = new HashMap<>();
        templateProperties.put("project", project);
        templateProperties.put("inputEncoding", getInputEncoding());
        templateProperties.put("outputEncoding", getOutputEncoding());
        // Put any of the properties in directly into the Velocity context
        for (final Map.Entry<Object, Object> entry : project.getProperties().entrySet()) {
            templateProperties.put((String) entry.getKey(), entry.getValue());
        }
        return templateProperties;
    }

}
