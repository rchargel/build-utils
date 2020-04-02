package com.github.rchargel.build.report

import org.custommonkey.xmlunit.XMLAssert
import org.custommonkey.xmlunit.XMLUnit
import org.junit.Test
import java.io.StringWriter
import java.nio.charset.StandardCharsets

class ReportGeneratorTest {

    @Test
    fun testReportGeneration() {
        val report = Section.builder("Report!!")
                .subTitle("Report sub title")
                .appendContent(Text(content = "This is my first report", title = "Title"))
                .appendContent(Section.builder("Part 1")
                        .appendContent(Text("dolor sit amet", "Lorem ipsum"))
                        .appendContent(Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."))
                        .build())
                .appendContent(Section.builder("Benchmark Tests and Evaluation Report")
                        .appendContent(Text("Complete report from the benchmark tests and evaluations."))
                        .appendContent(Table.builder()
                                .renderHeadings(false)
                                .headings(listOf("icon", "message", "number"))
                                .addCellValue("icon", Image.INFO_ICON)
                                .addCellValue("message", "Number of Tests")
                                .addCellValue("number", 4)
                                .build())
                        .appendContent(Section.builder("Hardware Specifications")
                                .appendContent(Table.builder()
                                        .headingsOnLeft(true)
                                        .addHeading("Hardware Model")
                                        .addCellValue("Hardware Model", "A Computer")
                                        .addHeading("Operating System")
                                        .addCellValue("Operating System", "An OS")
                                        .addHeading("CPU")
                                        .addCellValue("CPU", "A CPU")
                                        .build())
                                .build())
                        .appendContent(Section.builder("Evaluations")
                                .appendContent(Section.builder("Test Mode: AverageTime (Summary)")
                                        .appendContent(Table.builder()
                                                .tableName("AverageTime Test Class: com.fake.benchmarksExampleBenchmark")
                                                .headings(listOf("Test", "Performance"))
                                                .addRow(mapOf("Test" to "Benchmark Check Password [ Iterations = 10 ]", "Performance" to "151.51688 ms/op ± 66.06786"))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()

        val writer = StringWriter()
        writer.use {
            val reportGen = ReportGenerator()
            reportGen.writeReport(report, "1.0-SNAPSHOT", it)
        }

        val expected = Thread.currentThread().contextClassLoader.getResource("expected_report.html").readText(StandardCharsets.UTF_8)
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setNormalizeWhitespace(true)
        XMLAssert.assertXMLEqual(expected, writer.toString())
    }
}