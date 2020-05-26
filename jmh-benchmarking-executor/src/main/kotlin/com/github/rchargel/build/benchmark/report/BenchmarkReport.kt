package com.github.rchargel.build.benchmark.report

import com.github.rchargel.build.benchmark.results.BenchmarkResults
import com.github.rchargel.build.benchmark.results.BenchmarkTestResult
import com.github.rchargel.build.common.StringUtils.Companion.normalizeMemoryString
import com.github.rchargel.build.common.StringUtils.Companion.normalizeMetricString
import com.github.rchargel.build.report.*
import com.github.rchargel.build.report.chart.RawDataLineChartImageMaker
import java.awt.Color
import kotlin.math.max

class BenchmarkReport {
    companion object {
        private const val REPORT_TITLE = "report.title"
        private const val TABLE_OF_CONTENTS = "table.of.contents"
        private const val REPORT_DESCRIPTION = "report.description"
        private const val HARDWARE_SECTION_TITLE = "hardware.section.title"
        private const val HARDWARE_SECTION_HW_MODEL = "hardware.section.hardware.model"
        private const val HARDWARE_SECTION_OS = "hardware.section.operating.system"
        private const val HARDWARE_SECTION_CPU = "hardware.section.cpu"
        private const val HARDWARE_SECTION_CPU_ARCH = "hardware.section.cpu.architecture"
        private const val HARDWARE_SECTION_CPU_SPEED = "hardware.section.cpu.speed"
        private const val HARDWARE_SECTION_CPU_PHYSICAL = "hardware.section.cpu.physical"
        private const val HARDWARE_SECTION_CPU_LOGICAL = "hardware.section.cpu.logical"
        private const val HARDWARE_SECTION_MEMORY = "hardware.section.memory"
        private const val HARDWARE_SECTION_SWAP = "hardware.section.swap"
        private const val HARDWARE_SECTION_MEM_PAGESIZE = "hardware.section.memory.pagesize"
        private const val HARDWARE_SECTION_MEM_BANKS = "hardware.section.memory.banks"
        private const val HARDWARE_SECTION_MEM_BANK_LABEL = "hardware.section.memory.bank.label"
        private const val HARDWARE_SECTION_MEM_BANK_TYPE = "hardware.section.memory.bank.type"
        private const val HARDWARE_SECTION_MEM_BANK_CAPACITY = "hardware.section.memory.bank.capacity"
        private const val HARDWARE_SECTION_MEM_BANK_SPEED = "hardware.section.memory.bank.clockspeed"
        private const val ICON_TITLE = "icon.title"
        private const val MODE_TITLE = "mode.title"
        private const val TEST_TITLE = "test.title"
        private const val PVALUE_TITLE = "pvalue"
        private const val SUMMARY_SECTION_TITLE = "summary.section.title"
        private const val SUMMARY_SECTION_MODE_HEADING = "summary.section.mode.heading"
        private const val SUMMARY_SECTION_CLASS_HEADING = "summary.section.class.heading"
        private const val MESSAGE_TEST = "message.test"
        private const val MESSAGE_PERFORMANCE = "message.performance"
        private const val MESSAGE_CHART_DISTRIBUTION = "message.chart.distribution"
        private const val MESSAGE_CHART_EXECUTION = "message.chart.execution"
        private const val MESSAGE_CHART_ECDF = "message.chart.ecdf"
        private const val MESSAGE_CHART_RAW = "message.chart.raw"
        private const val MESSAGE_CHART_BASELINE_NAME = "message.chart.baseline.name"
        private const val MESSAGE_CHART_NAME = "message.chart.name"
        private const val MESSAGE_CHART_ECDF_AXIS = "message.chart.ecdf.axis"
        private const val MESSAGE_CHART_RAW_AXIS = "message.chart.raw.axis"

        @JvmStatic
        fun buildReport(testResults: BenchmarkResults, bundle: Messages) = Report.builder(bundle.text(REPORT_TITLE))
                .includeTOC(true)
                .tableOfContentsTitle(bundle.text(TABLE_OF_CONTENTS))
                .appendContent(Text(bundle.text(REPORT_DESCRIPTION)))
                .appendContent(createInfoTable(bundle, testResults))
                .appendContent(Section.builder(bundle.text(HARDWARE_SECTION_TITLE))
                        .appendContent(Table.builder()
                                .headingsOnLeft(true)
                                .addHeading(bundle.text(HARDWARE_SECTION_HW_MODEL))
                                .addCellValue(bundle.text(HARDWARE_SECTION_HW_MODEL), testResults.systemModel)
                                .addHeading(bundle.text(HARDWARE_SECTION_OS))
                                .addCellValue(bundle.text(HARDWARE_SECTION_OS), testResults.operatingSystem)
                                .addHeading(bundle.text(HARDWARE_SECTION_CPU))
                                .addCellValue(bundle.text(HARDWARE_SECTION_CPU), testResults.cpu)
                                .addHeading(bundle.text(HARDWARE_SECTION_CPU_ARCH))
                                .addCellValue(bundle.text(HARDWARE_SECTION_CPU_ARCH), testResults.architecture)
                                .addHeading(bundle.text(HARDWARE_SECTION_CPU_SPEED))
                                .addCellValue(bundle.text(HARDWARE_SECTION_CPU_SPEED), normalizeMetricString(testResults.cpuSpeedInHertz, "Hz"))
                                .addHeading(bundle.text(HARDWARE_SECTION_CPU_PHYSICAL))
                                .addCellValue(bundle.text(HARDWARE_SECTION_CPU_PHYSICAL), testResults.physicalProcessors)
                                .addHeading(bundle.text(HARDWARE_SECTION_CPU_LOGICAL))
                                .addCellValue(bundle.text(HARDWARE_SECTION_CPU_LOGICAL), testResults.logicalProcessors)
                                .addHeading(bundle.text(HARDWARE_SECTION_MEMORY))
                                .addCellValue(bundle.text(HARDWARE_SECTION_MEMORY), normalizeMemoryString(testResults.totalMemoryInBytes))
                                .addHeading(bundle.text(HARDWARE_SECTION_SWAP))
                                .addCellValue(bundle.text(HARDWARE_SECTION_SWAP), normalizeMemoryString(testResults.swapTotalInBytes))
                                .addHeading(bundle.text(HARDWARE_SECTION_MEM_PAGESIZE))
                                .addCellValue(bundle.text(HARDWARE_SECTION_MEM_PAGESIZE), normalizeMemoryString(testResults.memoryPageSizeInBytes))
                                .build())
                        .appendContent(Table.builder()
                                .tableName(bundle.text(HARDWARE_SECTION_MEM_BANKS))
                                .headings(listOf(
                                        bundle.text(HARDWARE_SECTION_MEM_BANK_LABEL),
                                        bundle.text(HARDWARE_SECTION_MEM_BANK_TYPE),
                                        bundle.text(HARDWARE_SECTION_MEM_BANK_CAPACITY),
                                        bundle.text(HARDWARE_SECTION_MEM_BANK_SPEED)
                                ))
                                .addRows(testResults.memoryBanks.map {
                                    mapOf(
                                            bundle.text(HARDWARE_SECTION_MEM_BANK_LABEL) to it.label,
                                            bundle.text(HARDWARE_SECTION_MEM_BANK_TYPE) to it.type,
                                            bundle.text(HARDWARE_SECTION_MEM_BANK_CAPACITY) to normalizeMemoryString(it.capacityInBytes),
                                            bundle.text(HARDWARE_SECTION_MEM_BANK_SPEED) to normalizeMetricString(it.clockSpeed, "Hz")
                                    )
                                })
                                .build())
                        .build())
                .appendContent(createEvaluations(testResults, bundle))

        private fun createInfoTable(bundle: Messages, testResults: BenchmarkResults): Table {
            val iconTitle = bundle.text(ICON_TITLE)
            val modeTitle = bundle.text(MODE_TITLE)
            val testTitle = bundle.text(TEST_TITLE)
            val pValueTitle = bundle.text(PVALUE_TITLE)
            val hasPValue = testResults.hasPValueResults

            val builder = Table.builder().headings(
                    if (hasPValue) listOf(iconTitle, modeTitle, testTitle, pValueTitle)
                    else listOf(iconTitle, modeTitle, testTitle)
            )
            groupByMode(testResults.results) { mode, results ->
                groupByClass(results) { className, classResults ->
                    classResults.sortedBy { it.methodName }.forEach {
                        val icon = if (it.pvalue == null) Image.INFO_ICON else if (it.pvalue >= testResults.minAllowedPValue) Image.SUCCESS_ICON else Image.ERROR_ICON
                        val test = "${className}.${it.key}"
                        val pValue = "%.4f".format(it.pvalue)
                        builder.addRow(if (hasPValue) mapOf(
                                iconTitle to icon,
                                modeTitle to mode,
                                testTitle to test,
                                pValueTitle to pValue
                        ) else mapOf(
                                iconTitle to icon,
                                modeTitle to mode,
                                testTitle to test
                        ))
                    }
                }
            }
            return builder.build()
        }

        private fun groupByMode(results: Collection<BenchmarkTestResult>?, modeConsumer: (mode: String, results: Collection<BenchmarkTestResult>) -> Unit) =
                results?.groupBy { it.mode }?.entries?.sortedBy { it.key }?.forEach { modeConsumer.invoke(it.key, it.value) }

        private fun groupByClass(results: Collection<BenchmarkTestResult>?, classConsumer: (className: String, results: Collection<BenchmarkTestResult>) -> Unit) =
                results?.groupBy { "${it.packageName}.${it.className}" }?.entries?.sortedBy { it.key }?.forEach { classConsumer.invoke(it.key, it.value) }


        private fun createEvaluations(testResults: BenchmarkResults, bundle: Messages): ReportContent {
            val builder = Section.builder(bundle.text(SUMMARY_SECTION_TITLE))

            val testHeading = bundle.text(MESSAGE_TEST)
            val perfHeading = bundle.text(MESSAGE_PERFORMANCE)
            val distHeading = bundle.text(MESSAGE_CHART_DISTRIBUTION)
            val ecdfHeading = bundle.text(MESSAGE_CHART_ECDF)
            val rawHeading = bundle.text(MESSAGE_CHART_RAW)

            groupByMode(testResults.results) { mode, modeValues ->
                val sectionBuilder = Section.builder(bundle.text(SUMMARY_SECTION_MODE_HEADING, mode))
                groupByClass(modeValues) { className, classValues ->
                    val tableBuilder = Table.builder()
                            .tableName(bundle.text(SUMMARY_SECTION_CLASS_HEADING, mode, className))
                            .headings(listOf(testHeading, perfHeading, distHeading, ecdfHeading, rawHeading))
                    classValues.sortedBy { it.methodName }.forEach { result ->
                        tableBuilder.addRow(mapOf(
                                testHeading to result.methodName,
                                perfHeading to "%.3f %s ± %.3f".format(result.distributionStatistics.mean, result.scoreUnits, result.meanErrorAt999),
                                distHeading to normalDistributionChart(result, bundle),
                                ecdfHeading to ecdfChart(result, bundle),
                                rawHeading to rawChart(result, bundle)
                        ))
                    }
                    sectionBuilder.appendContent(tableBuilder.build())
                }
                builder.appendContent(sectionBuilder.build())
            }
            return builder.build()
        }

        private fun ecdfChart(result: BenchmarkTestResult, bundle: Messages): Image {
            val chart = ECDFChartMaker(result.scoreUnits, bundle.text(MESSAGE_CHART_ECDF_AXIS))
                    .addDataset(bundle.text(MESSAGE_CHART_NAME), Color.blue, 2, result.rawMeasurementsWithoutOutliers.toDoubleArray())

            if (result.baselineMeasurements != null)
                chart.addDataset(bundle.text(MESSAGE_CHART_BASELINE_NAME), Color.red, 1, result.baselineMeasurementsWithoutOutliers?.toDoubleArray()!!)

            return chart.toImageBuilder(500, 300)
                    .title(bundle.text(MESSAGE_CHART_ECDF))
                    .thumbnail(true).build()
        }

        private fun rawChart(result: BenchmarkTestResult, bundle: Messages): Image {

            val chart = RawDataLineChartImageMaker(
                    bundle.text(MESSAGE_CHART_RAW_AXIS),
                    result.scoreUnits,
                    max(result.distributionStatistics.count.toInt(), result.baselineDistributionStatistics?.count?.toInt()
                            ?: -1)
            ).addDataset(bundle.text(MESSAGE_CHART_NAME), Color.blue, 2, result.rawMeasurements)

            if (result.baselineMeasurements != null)
                chart.addDataset(bundle.text(MESSAGE_CHART_BASELINE_NAME), Color.red, 1, result.baselineMeasurements)

            return chart.toImageBuilder(600, 300)
                    .title(bundle.text(MESSAGE_CHART_RAW))
                    .thumbnail(true).build()
        }

        private fun normalDistributionChart(result: BenchmarkTestResult, bundle: Messages): Image {
            val distHeading = bundle.text(MESSAGE_CHART_DISTRIBUTION)
            val chartName = bundle.text(MESSAGE_CHART_NAME)
            val baselineName = bundle.text(MESSAGE_CHART_BASELINE_NAME)

            val chart = BoxPlotChartImageMaker(bundle.text(MESSAGE_CHART_EXECUTION), result.scoreUnits)
                    .addDataset(chartName, Color.blue, 2, result.rawMeasurements)

            if (result.baselineDistributionStatistics != null)
                chart.addDataset(baselineName, Color.red, 1, result.baselineMeasurements!!)

            return chart.toImageBuilder(500, 300)
                    .title(distHeading)
                    .thumbnail(true)
                    .build()
        }
    }
}

