package com.github.rchargel.build.benchmark.maven.reports;

import com.github.rchargel.build.benchmark.results.BenchmarkTestResult;
import com.github.rchargel.build.report.AbstractSystemReportRenderer;
import org.apache.maven.doxia.sink.Sink;

public class SummaryTestRenderer implements EvaluationRenderer<BenchmarkTestResult> {

    private final AbstractSystemReportRenderer renderer;

    public SummaryTestRenderer(final AbstractSystemReportRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void startModeSection(final Sink sink, final String modeName) {
        sink.section3();
        sink.sectionTitle3();
        sink.text(renderer.getTranslatedText("summary.section.mode.heading", modeName));
        sink.sectionTitle3_();

        sink.table();
        sink.tableRow();
        renderer.renderCellHeaderText("message.test");
        renderer.renderCellHeaderText("message.performance");
        sink.tableRow_();
    }

    @Override
    public void endModeSection(final Sink sink) {
        sink.table_();
        sink.section3_();
    }

    @Override
    public void startClassSection(final Sink sink, final String modeName, final String className) {
        sink.tableRow();
        sink.tableHeaderCell(renderer.createColspan(2));
        sink.text(renderer.getTranslatedText("summary.section.class.heading", modeName, className));
        sink.tableHeaderCell_();
        sink.tableRow_();
    }

    @Override
    public void endClassSection(final Sink sink) {
        // not necessary
    }

    @Override
    public void evaluationSection(final Sink sink, final String modeName, final BenchmarkTestResult evaluation) {
        sink.tableRow();
        renderer.renderCellText(AbstractSystemReportRenderer.camelCaseToWords(evaluation.methodName));
        renderer.renderCellText(String.format("%s %s ± %s", renderer.getNumberFormat().format(evaluation.mean),
                evaluation.scoreUnits, renderer.getNumberFormat().format(evaluation.meanErrorAt999)));
        sink.tableRow_();
    }

}
