package com.github.rchargel.build.benchmark.report

import com.github.rchargel.build.benchmark.results.BenchmarkResults
import com.github.rchargel.build.report.*

class BenchmarkReport {
    companion object {
        @JvmStatic
        fun buildReport(testResults: BenchmarkResults, bundle: Messages) = ReportBuilder.newReport(bundle.text("report.title"))
                .includeTOC(true)
                .tableOfContentsTitle(bundle.text("table.of.contents"))
                .appendContent(Text(bundle.text("report.description")))
                .appendContent(Table.builder()
                        .renderHeadings(false)
                        .headings(listOf("icon", "name", "value"))
                        .addRow(mapOf(
                                "icon" to Image.INFO_ICON,
                                "name" to bundle.text("number.tests"),
                                "value" to (testResults.results?.size ?: 0)
                        ))
                        .build())
                .appendContent(Section.builder(bundle.text("hardware.section.title"))
                        .appendContent(Table.builder()
                                .headingsOnLeft(true)
                                .addHeading(bundle.text("hardware.section.hardware.model"))
                                .addCellValue(bundle.text("hardware.section.hardware.model"), testResults.systemModel)
                                .addHeading(bundle.text("hardware.section.operating.system"))
                                .addCellValue(bundle.text("hardware.section.operating.system"), testResults.operatingSystem)
                                .addHeading(bundle.text("hardware.section.cpu"))
                                .addCellValue(bundle.text("hardware.section.cpu"), testResults.cpu)
                                .addHeading(bundle.text("hardware.section.cpu.architecture"))
                                .addCellValue(bundle.text("hardware.section.cpu.architecture"), testResults.architecture)
                                .addHeading(bundle.text("hardware.section.cpu.physical"))
                                .addCellValue(bundle.text("hardware.section.cpu.physical"), testResults.physicalProcessors)
                                .addHeading(bundle.text("hardware.section.cpu.logical"))
                                .addCellValue(bundle.text("hardware.section.cpu.logical"), testResults.logicalProcessors)
                                .addHeading(bundle.text("hardware.section.memory"))
                                .addCellValue(bundle.text("hardware.section.memory"), testResults.totalMemoryInBytes)
                                .addHeading(bundle.text("hardware.section.swap"))
                                .addCellValue(bundle.text("hardware.section.swap"), testResults.swapTotalInBytes)
                                .build())
                        .build())
                .appendContent(createEvaluations(testResults, bundle))

        private inline fun createEvaluations(testResults: BenchmarkResults, bundle: Messages): ReportContent {
            val builder = Section.builder(bundle.text("summary.section.title"))

            val testHeading = bundle.text("message.test")
            val perfHeading = bundle.text("message.performance")

            testResults.results?.groupBy { it.mode }?.entries?.sortedBy { it.key }?.forEach { modeEntry ->
                val mode = modeEntry.key
                val sectionBuilder = Section.builder(bundle.text("summary.section.mode.heading", mode))
                modeEntry.value.groupBy { "${it.packageName}.${it.className}" }.entries.sortedBy { it.key }.forEach { classEntry ->
                    val className = classEntry.key
                    val tableBuilder = Table.builder()
                            .tableName(bundle.text("summary.section.class.heading", mode, className))
                            .headings(listOf(testHeading, perfHeading))
                    classEntry.value.sortedBy { it.methodName }.forEach { result ->
                        tableBuilder.addRow(mapOf(
                                testHeading to result.methodName,
                                perfHeading to "%.3f %s ± %.3f".format(result.distributionStatistics.mean, result.scoreUnits, result.meanErrorAt999)
                        ))
                    }
                    sectionBuilder.appendContent(tableBuilder.build())
                }
                builder.appendContent(sectionBuilder.build())
            }
            return builder.build()
        }
    }
}
