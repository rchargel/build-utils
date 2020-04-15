def writeSection = null
def writeTable = null
def writeContent = { element, level ->
    switch (element.class) {
        case com.github.rchargel.build.report.Text:
            if (element.title) {
                p { strong(element.title) }
            }
            p(element.content)
            break
        case com.github.rchargel.build.report.Image:
            img(title: element.title, src: element.dataURL, class: "${element.thumbnail ? 'thumbnail' : ''}")
            break
        case com.github.rchargel.build.report.Table:
            writeTable(element, level)
            break
        case com.github.rchargel.build.report.Section:
            writeSection(element, level+1)
            break
        default:
            p(element.class)
            break
    }
}
def writeTableValue = { element, level ->
    if (element == null) {
        yield("")
    } else {
        switch(element.class) {
            case com.github.rchargel.build.report.Image:
            case com.github.rchargel.build.report.Text:
            case com.github.rchargel.build.report.Table:
                writeContent(element, level)
                break
            default:
                yieldUnescaped(element)
                break
        }
    }
}
writeTable = { reportTable, level ->
    if (reportTable.tableName) {
        p { strong(reportTable.tableName) }
    }
    table(class:'bodyTable', border: '0') {
        if (reportTable.headingsOnLeft) {
            reportTable.headings.eachWithIndex { heading, index ->
                tr(class: "${index % 2 == 0 ? 'a' : 'b'}") {
                    th(style: 'width: 35%;', heading)
                    reportTable.rows.each { row ->
                        td { writeTableValue(row[heading], level) }
                    }
                }
            }
        } else {
            if (reportTable.renderHeadings) {
                tr(class: "b") {
                    reportTable.headings.each { th(it) }
                }
            }
            reportTable.rows.eachWithIndex { row, index ->
                tr(class: "${index % 2 == 0 ? 'a' : 'b' }") {
                    reportTable.headings.each { heading ->
                        td { writeTableValue(row[heading], level) }
                    }
                }
            }
        }
        if (reportTable.caption) {
            caption(reportTable.caption)
        }
    }
}
writeSection = { section, level ->
    if (section) {
        div(class: 'section') {
            switch (level) {
                case 1:
                    h2 { a(name: section.anchor, '') yield(section?.title) }
                    break
                case 2:
                    h3 { a(name: section.anchor, '') yield(section?.title) }
                    break
                case 3:
                    h4 { a(name: section.anchor, '') yield(section?.title) }
                    break
                case 4:
                    h5 { a(name: section.anchor, '') yield(section?.title) }
                    break
                default:
                    h6 { a(name: section.anchor, '') yield(section?.title) }
                    break
            }
            if (section?.subTitle) {
                p { strong(section?.subTitle) }
            }
            section?.content?.each { element ->
                if (element != null) {
                    writeContent(element, level)
                }
            }
        }
    }
}
yieldUnescaped('<!DOCTYPE html>')
html(lang:'en') {
    head {
        meta(charset: 'UTF-8')
        title(report.title)
        style(media: 'all', type: 'text/css') {
            yieldUnescaped("""<!--
${reportCSS}${customReportCSS}
-->""")
        }
        style(media: 'print', type: 'text/css') {
            yieldUnescaped("""<!--
${printCSS}${customPrintCSS}
-->""")
        }
    }
    body(class: 'composite') {
        div(id: 'banner') {
            div(class: 'clear') {
                hr()
            }
        }
        div(id: 'breadcrumbs') {
            div(class: 'xleft') {
                span(id: 'publishDate', "Last Published: $date")
                if (projectVersion) {
                    yield("|")
                    span(id: 'projectVersion', "Version: $projectVersion")
                }
            }
            div(class: 'xright', " ")
            div(class: 'clear') {
                hr()
            }
        }
        div(id: 'bodyColumn') {
            div(id: 'contextBox') {
                if (includeTOC) {
                    div(class: 'section') {
                        div(class: 'hideFromPrint') {
                            h2 {
                                a(name: 'Table_of_Contents', '')
                                yield(tocTitle)
                            }
                        }
                        div(id: 'tableofcontents', '')
                    }
                }
                writeSection(report, 1)
            }
        }
        script(type: 'text/javascript') {
            yieldUnescaped("""<!--
${javascript}
//-->""")
        }
    }
}