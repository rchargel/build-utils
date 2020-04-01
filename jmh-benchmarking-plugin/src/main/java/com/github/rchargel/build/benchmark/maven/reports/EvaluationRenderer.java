package com.github.rchargel.build.benchmark.maven.reports;

import com.github.rchargel.build.benchmark.results.BenchmarkTestResult;
import org.apache.maven.doxia.sink.Sink;

public interface EvaluationRenderer<R extends BenchmarkTestResult> {

    public void startModeSection(final Sink sink, final String modeName);

    public void endModeSection(final Sink sink);

    public void startClassSection(final Sink sink, final String modeName, final String className);

    public void endClassSection(final Sink sink);

    public void evaluationSection(final Sink sink, final String modeName, final R evaluation);

}
