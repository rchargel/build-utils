package net.zcarioca.maven.benchmark.reports;

import static java.util.Comparator.comparing;

import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.logging.Log;

import net.zcarioca.maven.AbstractSystemReportRenderer;
import net.zcarioca.maven.benchmark.results.BenchmarkResults;
import net.zcarioca.maven.benchmark.results.BenchmarkTestResult;

public class BenchmarkTestReport extends AbstractSystemReportRenderer {

    private static final String REPORT_DESCRIPTION = "report.description";
    private static final String NUM_TESTS = "number.tests";

    private static final String HARDWARE_SECTION_TITLE = "hardware.section.title";
    private static final String SUMMARY_SECTION_TITLE = "summary.section.title";
    private final BenchmarkResults results;

    public BenchmarkTestReport(final BenchmarkResults results, final Sink sink, final Log log, final Locale locale, final ResourceBundle bundle,
            final String encoding) {
        super(sink, log, locale, bundle, encoding);
        this.results = results;
    }

    @Override
    protected void renderBody() {
        sink.section1_();
        sink.section1();
        sink.sectionTitle1();
        sink.text(getTitle());
        sink.sectionTitle1_();

        sink.paragraph();
        sink.text(getTranslatedText(REPORT_DESCRIPTION));
        sink.paragraph_();

        final Map<String, Map<String, Map<String, Map<String, BenchmarkTestResult>>>> resultsMap = createHierarchy();

        sink.table();
        sink.tableRow();
        sink.tableCell();
        renderInfoIcon();
        sink.tableCell_();
        renderCellText(getTranslatedText(NUM_TESTS));
        renderCellText(numberFormat.format(results.size()));
        sink.tableRow_();
        sink.table_();

        renderSystemTable();

        sink.section2();
        sink.sectionTitle2();
        sink.text(getTranslatedText(SUMMARY_SECTION_TITLE));
        sink.sectionTitle2_();
        renderEvaluations(resultsMap, new SummaryTestRenderer(this));
        sink.section2_();
        sink.section1_();
    }

    private void renderSystemTable() {
        sink.section2();
        sink.sectionTitle2();
        sink.text(getTranslatedText(HARDWARE_SECTION_TITLE));
        sink.sectionTitle2_();

        sink.table();
        Optional.ofNullable(results.systemModel).ifPresent(value -> renderPropertyValueRow("hardware.section.hardware.model", value));
        Optional.ofNullable(results.operatingSystem).ifPresent(value -> renderPropertyValueRow("hardware.section.operating.system", value));
        Optional.ofNullable(results.cpu).ifPresent(value -> renderPropertyValueRow("hardware.section.cpu", value));
        Optional.ofNullable(results.architecture).ifPresent(value -> renderPropertyValueRow("hardware.section.cpu.architecture", value));
        Optional.ofNullable(results.physicalProcessors).ifPresent(value -> renderPropertyValueRow("hardware.section.cpu.physical", value.toString()));
        Optional.ofNullable(results.logicalProcessors).ifPresent(value -> renderPropertyValueRow("hardware.section.cpu.logical", value.toString()));
        Optional.ofNullable(results.totalMemoryInBytes)
                .ifPresent(value -> renderPropertyValueRow("hardware.section.memory", convertBytesToString(value)));
        Optional.ofNullable(results.swapTotalInBytes).ifPresent(value -> renderPropertyValueRow("Swap Memory", convertBytesToString(value)));
        sink.table_();

        sink.section2_();
    }

    private Map<String, Map<String, Map<String, Map<String, BenchmarkTestResult>>>> createHierarchy() {
        return results.results.stream().collect(Collectors.groupingBy(r -> r.mode,
                Collectors.groupingBy(r -> r.packageName,
                        Collectors.groupingBy(r -> r.className,
                                Collectors.toMap(r -> r.methodName, r -> r)))));
    }

    private <R extends BenchmarkTestResult> void renderEvaluations(final Map<String, Map<String, Map<String, Map<String, R>>>> evalMap,
            final EvaluationRenderer<R> evalRenderer) {
        evalMap.entrySet().stream().sorted(comparing(Entry::getKey)).forEach(modeEntry -> {
            evalRenderer.startModeSection(sink, modeEntry.getKey().toString());

            modeEntry.getValue().entrySet().stream().sorted(comparing(Entry::getKey)).flatMap(packageEntry -> {
                return packageEntry.getValue().entrySet().stream().sorted(comparing(Entry::getKey)).map(classEntry -> {
                    return new AbstractMap.SimpleEntry<>(packageEntry.getKey() + "." + classEntry.getKey(), classEntry.getValue());
                });
            }).forEach(classEntry -> {
                evalRenderer.startClassSection(sink, modeEntry.getKey(), classEntry.getKey());

                classEntry.getValue().values().stream().sorted(comparing(r -> r.methodName)).forEach(evaluation -> {
                    evalRenderer.evaluationSection(sink, modeEntry.getKey(), evaluation);
                });

                evalRenderer.endClassSection(sink);
            });

            evalRenderer.endModeSection(sink);
        });
    }

}
