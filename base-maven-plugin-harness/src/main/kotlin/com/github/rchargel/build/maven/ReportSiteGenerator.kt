package com.github.rchargel.build.maven

import com.github.rchargel.build.report.*
import org.apache.maven.doxia.markup.HtmlMarkup
import org.apache.maven.doxia.sink.Sink
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet

class ReportSiteGenerator(private val sink: Sink) {
    fun generateReport(report: Report) {
        sink.head()
        sink.title()
        sink.text(report.body.title)
        addStyleTag(sink, "all", "site.css")
        addStyleTag(sink, "print", "print-site.css")
        sink.title_()
        sink.head_()
        sink.body()
        if (report.includeTOC) {
            sink.section1()
            sink.unknown(DIV, arrayOf(HtmlMarkup.TAG_TYPE_START), attrs(CLASS, "hideFromPrint"))
            sink.sectionTitle1()
            sink.text(report.tableOfContentsTitle)
            sink.sectionTitle1_()
            sink.unknown(DIV, arrayOf(HtmlMarkup.TAG_TYPE_END), null)
            sink.unknown(DIV, arrayOf(HtmlMarkup.TAG_TYPE_START), attrs(ID, "tableofcontents"))
            sink.unknown(DIV, arrayOf(HtmlMarkup.TAG_TYPE_END), null)
            sink.section1_()
        }
        renderContent(report.body, 1)
        addJavascript(sink, "report.js")
        sink.body_()
    }

    private fun renderContent(content: Any?, level: Int) {
        if (content != null) {
            when (content) {
                is Section -> renderSection(content, level)
                is Image -> renderImage(content)
                is Text -> renderText(content)
                is Table -> renderTable(content, level)
                else -> sink.rawText(content.toString())
            }
        }
    }

    private fun renderSection(content: Section, level: Int) {
        when (level) {
            1 -> {
                sink.section1()
                sink.sectionTitle1()
                sink.text(content.title)
                sink.sectionTitle1_()
            }
            2 -> {
                sink.section2()
                sink.sectionTitle2()
                sink.text(content.title)
                sink.sectionTitle2_()
            }
            3 -> {
                sink.section3()
                sink.sectionTitle3()
                sink.text(content.title)
                sink.sectionTitle3_()
            }
            4 -> {
                sink.section4()
                sink.sectionTitle4()
                sink.text(content.title)
                sink.sectionTitle4_()
            }
            5 -> {
                sink.section5()
                sink.sectionTitle5()
                sink.text(content.title)
                sink.sectionTitle5_()
            }
            else -> {
                sink.section6()
                sink.sectionTitle6()
                sink.text(content.title)
                sink.sectionTitle6_()
            }
        }
        boldText(content.subTitle)
        content.content.forEach { renderContent(it, level + 1) }
        when (level) {
            1 -> sink.section1_()
            2 -> sink.section2_()
            3 -> sink.section3_()
            4 -> sink.section4_()
            5 -> sink.section5_()
            else -> sink.section6_()
        }
    }

    private fun renderImage(content: Image) {
        if (content.thumbnail)
            sink.figureGraphics(content.dataURL, attrs(arrayOf(CLASS, "thumbnail", TITLE, content.title.orEmpty())))
        else
            sink.figureGraphics(content.dataURL, attrs(TITLE, content.title.orEmpty()))
    }

    private fun renderText(content: Text) {
        boldText(content.title)
        sink.paragraph()
        sink.rawText(content.content)
        sink.paragraph_()
    }

    private fun renderTable(content: Table, level: Int) {
        boldText(content.tableName)
        sink.table()
        if (content.headingsOnLeft) {
            renderVerticalTable(content, level)
        } else {
            renderHorizontalTable(content, level)
        }
        if (content.caption != null) {
            sink.tableCaption()
            sink.text(content.caption)
            sink.tableCaption_()
        }
        sink.table_()
    }

    private fun boldText(text: String?) {
        if (text != null) {
            sink.paragraph()
            sink.bold()
            sink.rawText(text)
            sink.bold_()
            sink.paragraph_()
        }
    }

    private fun renderVerticalTable(table: Table, level: Int) {
        for (heading in table.headings) {
            sink.tableRow()
            sink.tableHeaderCell(attrs(STYLE, "width: 35%;"))
            sink.text(heading)
            sink.tableHeaderCell_()
            for (row in table.rows) {
                sink.tableCell()
                renderContent(row[heading], level)
            }
            sink.tableRow_()
        }
    }

    private fun renderHorizontalTable(table: Table, level: Int) {
        if (table.renderHeadings) {
            sink.tableRow()
            for (heading in table.headings) {
                sink.tableHeaderCell()
                sink.text(heading)
                sink.tableHeaderCell_()
            }
            sink.tableRow_()
        }
        for (row in table.rows) {
            sink.tableRow()
            for (heading in table.headings) {
                sink.tableCell()
                renderContent(row[heading], level)
                sink.tableCell_()
            }
            sink.tableRow_()
        }
    }

    private fun attrs(name: String, value: String): SinkEventAttributeSet {
        val attrs = SinkEventAttributeSet()
        attrs.addAttribute(name, value)
        return attrs
    }

    private fun attrs(nameValues: Array<String>): SinkEventAttributeSet {
        val attrs = SinkEventAttributeSet()
        for (i in nameValues.indices step 2) {
            attrs.addAttribute(nameValues[i], nameValues[i + 1])
        }
        return attrs
    }

    private fun addJavascript(sink: Sink, scriptName: String) {
        val attrs = SinkEventAttributeSet()
        attrs.addAttribute(TYPE, "text/javascript")
        sink.unknown(SCRIPT, arrayOf(HtmlMarkup.TAG_TYPE_START), attrs(TYPE, "text/javascript"))
        sink.comment("""
            ${Report.readCompressed(scriptName)}
        //""")
        sink.unknown(SCRIPT, arrayOf(HtmlMarkup.TAG_TYPE_END), null)
    }

    private fun addStyleTag(sink: Sink, media: String, scriptName: String) {
        val attrs = SinkEventAttributeSet()
        attrs.addAttribute(TYPE, "text/css")
        attrs.addAttribute(MEDIA, media)
        sink.unknown(STYLE, arrayOf(HtmlMarkup.TAG_TYPE_START), attrs)
        sink.comment("""
            ${Report.readCompressed(scriptName)}
        """)
        sink.unknown(STYLE, arrayOf(HtmlMarkup.TAG_TYPE_END), null)
    }

    companion object {
        const val DIV = "div"
        const val SCRIPT = "script"
        const val STYLE = "style"
        const val CDATA = "cdata"
        const val MEDIA = "media"
        const val TYPE = "type"
        const val TITLE = "title"
        const val CLASS = "class"
        const val ID = "id"
    }
}