package com.github.rchargel.build.report

import java.io.IOException
import java.io.Writer
import java.time.LocalDate

class Report internal constructor(
        val body: Section,
        val publishDate: LocalDate,
        val projectVersion: String,
        val includeTOC: Boolean,
        val tableOfContentsTitle: String
) {
    private val reportGenerator = ReportGenerator()

    @Throws(IOException::class)
    fun writeReportTo(writer: Writer): Unit {
        reportGenerator.writeReport(mapOf(
                "report" to body,
                "date" to publishDate,
                "projectVersion" to projectVersion,
                "includeTOC" to includeTOC,
                "tocTitle" to tableOfContentsTitle
        ), writer)
    }

    class ReportBuilder internal constructor(
            private val sectionBuilder: Section.Builder,
            private var publishDate: LocalDate = LocalDate.now(),
            private var includeTOC: Boolean = true,
            private var tableOfContentsTitle: String = "Table of Contents",
            private var projectVersion: String? = null
    ) {
        fun projectVersion(projectVersion: String) = apply { this.projectVersion = projectVersion }
        fun appendContent(content: ReportContent) = apply { this.sectionBuilder.appendContent(content) }
        fun subReportTitle(subTitle: String) = apply { this.sectionBuilder.subTitle(subTitle) }
        fun publishDate(publishDate: LocalDate) = apply { this.publishDate = publishDate }
        fun includeTOC(includeTOC: Boolean) = apply { this.includeTOC = includeTOC }
        fun tableOfContentsTitle(tableOfContentsTitle: String) = apply { this.tableOfContentsTitle = tableOfContentsTitle }
        fun build() = Report(
                sectionBuilder.build(),
                publishDate,
                projectVersion.orEmpty(),
                includeTOC,
                tableOfContentsTitle.orEmpty()
        )
    }

    companion object {
        @JvmStatic
        fun builder(title: String) = ReportBuilder(Section.builder(title))
    }
}