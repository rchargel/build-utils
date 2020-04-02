package com.github.rchargel.build.report

import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import java.io.InputStreamReader
import java.io.Writer
import java.time.LocalDate

/**
 * Utility class used to write HTML reports
 */
class ReportGenerator {
    private val templateEngine: MarkupTemplateEngine
    private val template: Template

    init {
        val config = TemplateConfiguration()
        config.isUseDoubleQuotes = true
        templateEngine = MarkupTemplateEngine(config)

        var tmplt: Template? = null
        Thread.currentThread().contextClassLoader.getResourceAsStream("report.tpl").use {
            tmplt = templateEngine.createTemplate(InputStreamReader(it))
        }
        this.template = tmplt ?: throw IllegalStateException("Template not initialized")
    }

    /**
     * Writes a [report] with the given [projectVersion] to the provided [output].
     */
    fun writeReport(report: Section, projectVersion: String? = null, output: Writer) {
        template.make(mapOf(
                "report" to report,
                "date" to LocalDate.now(),
                "projectVersion" to projectVersion
        )).writeTo(output)
    }
}