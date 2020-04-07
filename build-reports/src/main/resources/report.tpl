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
            img(title: element.title, src: element.dataURL)
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
                yield(element)
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
        style(media: 'all', type: 'text/css', """
body { margin: 0px; padding: 0px; }
img { border:none; }
table {
  padding:0px;
  width: 100%;
  margin-left: -2px;
  margin-right: -2px;
}
acronym { cursor: help; border-bottom: 1px dotted #feb; }
table.bodyTable th, table.bodyTable td { padding: 2px 4px 2px 4px; vertical-align: top; }
div.clear{ clear:both; visibility: hidden; }
div.clear hr{ display: none; }
#bannerLeft, #bannerRight { font-size: xx-large; font-weight: bold; }
#bannerLeft img, #bannerRight img { margin: 0px; }
.xleft, #bannerLeft img { float:left; }
.xright, #bannerRight { float:right; }
#banner { padding: 0px; }
#banner img { border: none; }
#breadcrumbs { padding: 3px 10px 3px 10px; }
#leftColumn { width: 170px; float:left; overflow: auto; }
#bodyColumn { margin-right: 1.5em; margin-left: 197px; }
#legend { padding: 8px 0 8px 0; }
#navcolumn { padding: 8px 4px 0 8px; }
#navcolumn h5 { margin: 0; padding: 0; font-size: small; }
#navcolumn ul { margin: 0; padding: 0; font-size: small; }
#navcolumn li {
  list-style-type: none;
  background-image: none;
  background-repeat: no-repeat;
  background-position: 0 0.4em;
  padding-left: 16px;
  list-style-position: outside;
  line-height: 1.2em;
  font-size: smaller;
}
#navcolumn li.expanded { /* background-image: url(../images/expanded.gif); */ }
#navcolumn li.collapsed { /* background-image: url(../images/collapsed.gif); */ }
#navcolumn li.none { text-indent: -1em; margin-left: 1em; }
#poweredBy { text-align: center; }
#navcolumn img { margin-top: 10px; margin-bottom: 3px; }
#poweredBy img { display:block; margin: 20px 0 20px 17px; }
#search img { margin: 0px; display: block; }
#search #q, #search #btnG { border: 1px solid #999; margin-bottom:10px; }
#search form { margin: 0px; }
#lastPublished { font-size: x-small; }
.navSection { margin-bottom: 2px; padding: 8px; }
.navSectionHead { font-weight: bold; font-size: x-small; }
.section { padding: 4px; }
#footer { padding: 3px 10px 3px 10px; font-size: x-small; }
#breadcrumbs { font-size: x-small; margin: 0pt; }
.source { padding: 12px; margin: 1em 7px 1em 7px; }
.source pre { margin: 0px; padding: 0px; }
#navcolumn img.imageLink, .imageLink {
  padding-left: 0px;
  padding-bottom: 0px;
  padding-top: 0px;
  padding-right: 2px;
  border: 0px;
  margin: 0px;
}
body { padding: 0px 0px 10px 0px; }
body, td, select, input, li{ font-family: Verdana, Helvetica, Arial, sans-serif; font-size: 13pt; }
code{ font-family: Courier, monospace; font-size: 13px; }
a { text-decoration: none; }
a:link { color:#36a; }
a:visited  { color:#47a; }
a:active, a:hover { color:#69c; }
#legend li.externalLink { /* background: url(../images/external.png) left top no-repeat; */ padding-left: 18px; }
a.externalLink, a.externalLink:link, a.externalLink:visited, a.externalLink:active, a.externalLink:hover {
  /* background: url(../images/external.png) right center no-repeat; */
  padding-right: 18px;
}
#legend li.newWindow { /* background: url(../images/newwindow.png) left top no-repeat; */ padding-left: 18px;
}
a.newWindow, a.newWindow:link, a.newWindow:visited, a.newWindow:active, a.newWindow:hover {
  /* background: url(../images/newwindow.png) right center no-repeat; */
  padding-right: 18px;
}
h2 {
  padding: 4px 4px 4px 6px;
  border: 1px solid #999;
  color: #900;
  background-color: #ddd;
  font-weight:900;
  font-size: xx-large;
}
h3 {
  padding: 4px 4px 4px 6px;
  border: 1px solid #aaa;
  color: #900;
  background-color: #eee;
  font-weight: normal;
  font-size: x-large;
}
h4 {
  padding: 4px 4px 4px 6px;
  border: 1px solid #bbb;
  color: #900;
  background-color: #fff;
  font-weight: normal;
  font-size: large;
}
h5 {
  padding: 4px 4px 4px 6px;
  color: #900;
  font-size: large;
}
h6 {
  font-weight: normal;
  font-style: italic;
  font-size: large;
}
p { line-height: 1.3em; font-size: small; }
#breadcrumbs {
  border-top: 1px solid #aaa;
  border-bottom: 1px solid #aaa;
  background-color: #ccc;
}
#leftColumn {
  margin: 10px 0 0 5px;
  border: 1px solid #999;
  background-color: #eee;
}
#navcolumn h5 {
  font-size: smaller;
  border-bottom: 1px solid #aaaaaa;
  padding-top: 2px;
  color: #000;
}
table.bodyTable th {
  color: white;
  background-color: #bbb;
  text-align: left;
  font-weight: bold;
}
table.bodyTable th, table.bodyTable td { font-size: 1em; }
table.bodyTable tr.a { background-color: #ddd; }
table.bodyTable tr.b { background-color: #eee; }
.source { border: 1px solid #999; }
dl {
  padding: 4px 4px 4px 6px;
  border: 1px solid #aaa;
  background-color: #ffc;
}
dt { color: #900; }
#organizationLogo img, #projectLogo img, #projectLogo span{ margin: 8px; }
#banner { border-bottom: 1px solid #fff; }
#bodyColumn { margin-left: 1.5em; }
.raw-values-pane { height: 300px; width: 100%; overflow: auto; }
a.toggle { color:#ffffff; cursor:pointer;}
a.toggle:hover { text-decoration: underline; }
ol { counter-reset: item }
li{ display: block }
li:before { content: counters(item, ".") " "; counter-increment: item }
.showInPrintOnly { display: none; }
""")
        style(media: 'print', type: 'text/css', """
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
#banner, #footer, #leftcol, #breadcrumbs, .docs #toc, .docs .courtesylinks, #leftColumn, #navColumn { display: none !important; }
#bodyColumn, body.docs div.docs { margin: 0 !important; border: none !important }
a.toggle:hover { text-decoration: none; }
a.toggle:before { content: ''; }
a.toggle.active:before { content: ''; }
#tableofcontents, .hideFromPrint { display:none !important; }
.showInPrintOnly { display: block !important; }
table th, table td { border: 1px solid #666; background-color: transparent; }
table.bodyTable th { color: #000; font-weight: bold; }
.pageBreak { page-break-before: always; }
""")
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
        yieldUnescaped("""//<![CDATA[
var loadTag = function(element, tag) {var i = 0, subElements = element.getElementsByTagName(tag);for (; i < subElements.length; i++) {if (subElements[i]) {return subElements[i];} } return null; };var loadLevel= function(element, level) {var header, a, text, listText = '', i = 0, headers = element.getElementsByTagName("h" + level);if (headers.length > 0) {listText += '<ol>';for (; i < headers.length; i++) {header = headers[i];a = loadTag(header, 'a');text = header.innerText || header.textContent;listText += '<li><a href="#' + a.getAttribute('name') + '">' + text + '</a>';if (level < 5) {listText += loadLevel(header.parentElement, level + 1);} listText += '</li>'; } listText += '</ol>'; } return listText; };(function() {var toc = document.getElementById('tableofcontents');toc.innerHTML = loadLevel(document, 2);})();
//]]>""")
        }
    }
}