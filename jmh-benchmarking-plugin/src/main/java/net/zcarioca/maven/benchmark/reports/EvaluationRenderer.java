package net.zcarioca.maven.benchmark.reports;

import org.apache.maven.doxia.sink.Sink;

import net.zcarioca.maven.benchmark.results.BenchmarkTestResult;

public interface EvaluationRenderer<R extends BenchmarkTestResult> {

    public void startModeSection(final Sink sink, final String modeName);

    public void endModeSection(final Sink sink);

    public void startClassSection(final Sink sink, final String modeName, final String className);

    public void endClassSection(final Sink sink);

    public void evaluationSection(final Sink sink, final String modeName, final R evaluation);

}
