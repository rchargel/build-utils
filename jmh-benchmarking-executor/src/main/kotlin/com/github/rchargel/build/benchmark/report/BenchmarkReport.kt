package com.github.rchargel.build.benchmark.report

import com.github.rchargel.build.benchmark.results.BenchmarkResults
import com.github.rchargel.build.common.StringUtils.Companion.normalizeMemoryString
import com.github.rchargel.build.common.StringUtils.Companion.normalizeMetricString
import com.github.rchargel.build.report.*
import com.github.rchargel.build.report.chart.RawDataLineChartImageMaker
import java.awt.Color

class BenchmarkReport {
    companion object {
        @JvmStatic
        fun buildReport(testResults: BenchmarkResults, bundle: Messages) = Report.builder(bundle.text("report.title"))
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
                                .addHeading(bundle.text("hardware.section.cpu.speed"))
                                .addCellValue(bundle.text("hardware.section.cpu.speed"), normalizeMetricString(testResults.cpuSpeedInHertz, "Hz"))
                                .addHeading(bundle.text("hardware.section.cpu.physical"))
                                .addCellValue(bundle.text("hardware.section.cpu.physical"), testResults.physicalProcessors)
                                .addHeading(bundle.text("hardware.section.cpu.logical"))
                                .addCellValue(bundle.text("hardware.section.cpu.logical"), testResults.logicalProcessors)
                                .addHeading(bundle.text("hardware.section.memory"))
                                .addCellValue(bundle.text("hardware.section.memory"), normalizeMemoryString(testResults.totalMemoryInBytes))
                                .addHeading(bundle.text("hardware.section.swap"))
                                .addCellValue(bundle.text("hardware.section.swap"), normalizeMemoryString(testResults.swapTotalInBytes))
                                .addHeading(bundle.text("hardware.section.memory.pagesize"))
                                .addCellValue(bundle.text("hardware.section.memory.pagesize"), normalizeMemoryString(testResults.memoryPageSizeInBytes))
                                .build())
                        .appendContent(Table.builder()
                                .tableName(bundle.text("hardware.section.memory.banks"))
                                .headings(listOf(
                                        bundle.text("hardware.section.memory.bank.label"),
                                        bundle.text("hardware.section.memory.bank.type"),
                                        bundle.text("hardware.section.memory.bank.capacity"),
                                        bundle.text("hardware.section.memory.bank.clockspeed")
                                ))
                                .addRows(testResults.memoryBanks.map {
                                    mapOf(
                                            bundle.text("hardware.section.memory.bank.label") to it.label,
                                            bundle.text("hardware.section.memory.bank.type") to it.type,
                                            bundle.text("hardware.section.memory.bank.capacity") to normalizeMemoryString(it.capacityInBytes),
                                            bundle.text("hardware.section.memory.bank.clockspeed") to normalizeMetricString(it.clockSpeed, "Hz")
                                    )
                                })
                                .build())
                        .build())
                .appendContent(createEvaluations(testResults, bundle))

        private fun createEvaluations(testResults: BenchmarkResults, bundle: Messages): ReportContent {
            val builder = Section.builder(bundle.text("summary.section.title"))

            val testHeading = bundle.text("message.test")
            val perfHeading = bundle.text("message.performance")
            val distHeading = bundle.text("message.chart.distribution")
            val ecdfHeading = bundle.text("message.chart.ecdf")
            val rawHeading = bundle.text("message.chart.raw")

            val chartName = bundle.text("message.chart.name")
            val distAxis = bundle.text("message.chart.distribution.axis")
            val ecdfAxis = bundle.text("message.chart.ecdf.axis")
            val rawAxis = bundle.text("message.chart.raw.axis")

            testResults.results?.groupBy { it.mode }?.entries?.sortedBy { it.key }?.forEach { modeEntry ->
                val mode = modeEntry.key
                val sectionBuilder = Section.builder(bundle.text("summary.section.mode.heading", mode))
                modeEntry.value.groupBy { "${it.packageName}.${it.className}" }.entries.sortedBy { it.key }.forEach { classEntry ->
                    val className = classEntry.key
                    val tableBuilder = Table.builder()
                            .tableName(bundle.text("summary.section.class.heading", mode, className))
                            .headings(listOf(testHeading, perfHeading, distHeading, ecdfHeading, rawHeading))
                    classEntry.value.sortedBy { it.methodName }.forEach { result ->
                        tableBuilder.addRow(mapOf(
                                testHeading to result.methodName,
                                perfHeading to "%.3f %s ± %.3f".format(result.distributionStatistics.mean, result.scoreUnits, result.meanErrorAt999),
                                distHeading to NormalDistributionChartMaker(result.scoreUnits, distAxis, result.distributionStatistics.minimum, result.distributionStatistics.maximum)
                                        .addDataset(chartName, Color.blue, 1, result.distributionStatistics)
                                        .toImageBuilder(500, 300)
                                        .title(distHeading)
                                        .thumbnail(true).build(),
                                ecdfHeading to ECDFChartMaker(result.scoreUnits, ecdfAxis)
                                        .addDataset(chartName, Color.blue, 1, result.rawMeasurements.toDoubleArray())
                                        .toImageBuilder(500, 300)
                                        .title(ecdfHeading)
                                        .thumbnail(true).build(),
                                rawHeading to RawDataLineChartImageMaker(rawAxis, result.scoreUnits, result.distributionStatistics.count.toInt())
                                        .addDataset(chartName, Color.blue, 1, result.rawMeasurements)
                                        .toImageBuilder(600, 300)
                                        .title(rawHeading)
                                        .thumbnail(true).build()
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

