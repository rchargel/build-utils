package com.github.rchargel.build.benchmark.report

import com.fake.test.SimpleBenchmark
import com.github.rchargel.build.benchmark.BenchmarkExecutor
import com.github.rchargel.build.report.Messages
import com.github.rchargel.build.test.ClassLoaderHelper
import org.junit.Test
import java.util.*


class BenchmarkReportTest {
    companion object {
        init {
            ClassLoaderHelper.addClassToClassLoader(SimpleBenchmark::class.java)
        }
    }

    @Test
    fun canBuildBenchmarkReport() {
        val benchmarks = BenchmarkExecutor().executeBenchmarks(0.0, 1)
        val report = BenchmarkReport.buildReport(benchmarks, Messages.loadMessages("messages", Locale.US))
                .build()

        assert(report != null) { "Report was null" }

        val comparison = BenchmarkExecutor().executeBenchmarks(1.0, 1).compareToBaseline(benchmarks, true)
        val comparisonReport = BenchmarkReport.buildReport(comparison, Messages.loadMessages("messages", Locale.US))
                .build()

        assert(comparisonReport != null) { "Comparison is null" }
        assert(comparisonReport.includeTOC) { "No TOC" }
        assert(comparisonReport.projectVersion == null) { "No project version should exist" }
        assert(comparisonReport.tableOfContentsTitle == "Table of Contents") { "TOC title was ${comparisonReport.tableOfContentsTitle}" }
    }
}