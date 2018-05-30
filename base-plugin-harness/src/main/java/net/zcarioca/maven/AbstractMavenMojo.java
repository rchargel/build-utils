package net.zcarioca.maven;

import static net.dempsy.util.Functional.recheck;
import static net.dempsy.util.Functional.uncheck;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;

public abstract class AbstractMavenMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Component
    protected DependencyGraphBuilder graphBuilder;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${basedir}")
    protected File baseDirectory;

    @Parameter(defaultValue = "${project.build.directory}")
    protected File buildDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    protected File classesDirectory;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}")
    protected File testClassesDirectory;

    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", readonly = true)
    protected String inputEncoding;

    @Parameter(property = "outputEncoding", defaultValue = "${project.reporting.outputEncoding}", readonly = true)
    protected String outputEncoding;

    @Parameter(property = "project.artifactMap", readonly = true, required = true)
    protected Map<String, Artifact> projectArtifactMap;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final URL[] classes = scanClasses();
        final ClassLoader customContextClassLoader = URLClassLoader.newInstance(classes, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(customContextClassLoader);

        executeMojo();
    }

    protected abstract void executeMojo() throws MojoExecutionException, MojoFailureException;

    protected static String getResourceAsFile(final String resourceName) throws Exception {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final String findName = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;

        final Enumeration<URL> urls = loader.getResources(findName);
        while (urls.hasMoreElements()) {
            final URI url = urls.nextElement().toURI();
            if (!url.getScheme().toLowerCase().startsWith("jar")) {
                final File file = new File(url);
                if (file != null || file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        throw new RuntimeException("Unable to find resource " + resourceName);
    }

    protected URL[] scanClasses() throws MojoExecutionException {
        final Set<Artifact> dependencySet = new HashSet<>();
        try {
            final DefaultProjectBuildingRequest request = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
            request.setProject(project);
            final DependencyNode node = graphBuilder.buildDependencyGraph(request, null);

            buildArtifactSet(node, dependencySet);
        } catch (final Exception e) {
            throw new MojoExecutionException("Could not build classpath", e);

        }
        // testing for nulls along the chain, ugly but may be necessary.

        final Set<URL> urlSet = recheck(() -> dependencySet.stream().filter(Objects::nonNull)
                .map(Artifact::getFile).filter(Objects::nonNull).filter(file -> !file.isDirectory())
                .map(File::toURI).filter(Objects::nonNull)
                .map(uri -> uncheck(() -> uri.toURL())).filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        try {
            if (classesDirectory.exists()) {
                urlSet.add(classesDirectory.toURI().toURL());
            }
            if (testClassesDirectory.exists()) {
                urlSet.add(testClassesDirectory.toURI().toURL());
            }
        } catch (final Exception e) {}

        final URL[] urls = urlSet.toArray(new URL[urlSet.size()]);

        return urls;
    }

    private void buildArtifactSet(final DependencyNode node, final Set<Artifact> artifacts) {
        if (node.getArtifact() != null) {
            if (!artifacts.contains(node.getArtifact())) {
                artifacts.add(node.getArtifact());
                Optional.ofNullable(node.getChildren()).ifPresent(list -> {
                    list.stream().forEach(n -> buildArtifactSet(n, artifacts));
                });
            }
        }
    }

}
