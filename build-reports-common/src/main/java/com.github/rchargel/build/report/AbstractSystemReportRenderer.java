package com.github.rchargel.build.report;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.reporting.AbstractMavenReportRenderer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractSystemReportRenderer extends AbstractMavenReportRenderer {

    private static final String[] ABBREVIATIONS = new String[]{
            "B",
            "KB",
            "MB",
            "GB",
            "TB"
    };

    private static final long TEN_24 = 1024L;
    private static final long LIMIT = TEN_24 * 2;

    private static final String ERROR_GIF = "data:image/gif;base64,R0lGODlhDwAPAPcAAAAAAP///40LEbIOFbEOFasOFaMNFHEJDXgKD3YKD20XG7g7QJJHSmY0NunS018HDMEPGLcOFp0MFJwME5sME4IKEGkIDVsHDL4PGL0PGLkPGLcPGKsOFqYNFZgME5cME5YME5MME4oLEogLEX4KEH0KEHMJD3EJDnAJDm4JDlgHC1YHDEwGCp4NFJ0NFYULEYMLEYELEXYKEGwJDr4RG34LEcIUH64SHMIWIrEiLKI5P4I2O3s2Oo5kZuWztvDg4cIXJL8ZJ78aKalPV82xs97Q0cUdLcUfL6scKsciNMAiM9hzffTW2fHV2PDW2frw8fvy88clOMcmOsgoPcgqQMgrQcMqP8k1SL09TcBNXPnx8sQrQskuRskvR8kwScsyTcszT8gyTsc8U/TY3cs1Usw2U8w3Vd6ClMw6Wcs6Wc08XMw7W809Xs0/Yc1AYs9CZfPv8EggMUsiNEghM0YgMUkiNGcxTG05WHJAYnhIbj4lOUouRq9wq7Fyrk4zT7J2tLN4t7V7u0AtRUw2Uk84VjAbOEczTzEcOmJHbTMdPTQePjcgQzUfQWdPeD8yTUU3VG1XhnxkmX1nnY51spqEwV5Wg/jw8Ojg4Pbw8P39/fv7+/r6+vT09P///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAJ0ALAAAAAAPAA8AAAjPADsJHEiwIEFKgQD96cNnksFOkt60WWMGjJcqjQpGcsMGTZkvXahISYJoICQ1ac6MEbPlCpMlQoD46VTJDJkwUAI4wdIkwJMbGzIsyuOFi5UsWgIotaQDRIcBjPBMiaIEyRClARgcqEChgKI7R4wEyeEAaxEFJDwYSGQHhw0aPgJg6gEnAJESIVwcMgQBg4YFP3awaHCJhwwBHwp12hOBQIsKFi6oWPHgBAw9AwlxmDACgQkUKWYkEFRwkIQQIka8iFGDtMFHcurMiUPHEcGAADs=";
    private static final String SUCCESS_GIF = "data:image/gif;base64,R0lGODlhDwAPAPcAAAAAAP///4SZuqa2zqe3zrzI2sfR4L/I1WN+oqGxx9/l7XeQrH+ZtKa4y5euv52zw6m9y1Nxgh0qL4KhrRolKCM4PRsnKiEzNx0sLyhDSCdBRiY+Qh4vMh0qLClGSRciIVBya5y8tKLAuGqVhoSsnHSci6LFskZjT4OtjzdHO8XWyJ3Io6zWqbfbtLjfsjJCLrLhpbPiprfjq7jkrLjkrbrlr7zlsY/CgLbjqbLgorLgo5zOiKzemKvcmanZl6vbl12VQp3OhaLUi6nbkZ3NhK3Vl12UPqHUhOfx4Yi7ZpPHco2/bYi2atDmwvj79mKcN0p3KlWHMFeKMpXRadTkyG2qO0FnJHKzP2+sPWWeOGaeOGKbN1qOMlmKMVOCLkp0KVF5MYnLV4W9WXWkUWWLRmOIRZe5e5u9gXuTaJ2tkM7Xx+7z6oTKR3a3QHW1P2ulOmieN2CSM0pyKH/DRV6RM1F8LFN+LURoJX28REVpJovOUbLFotbgzc/XyOPr3EltJlF4K090KU1vKOrv5UxsJklnJUdjJPX38v7+/vv7+/f39////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAIsALAAAAAAPAA8AAAi7ABcJHEiwoEEIIR4YLFigRQ0WBBYOFGFDRg4SEhc1cEEDxo8dCxYqMDEjhg8iTlQcMOgAhw4eQZgEoBKhoIEVPYYcWbImgBkgCQhOEFKkSZIxAQbFwQJi4IAbSpAE8MMnwB46b4wIEDhiipgzhwKILcNly5MTixiE0YMHDpk+AdR8icKlCxgEKNjMcaPFi5U0aOTUsQMo0IsSba5UySIFSp47fwQRKmQoxSIPGTRsqHCBAwYJHSxQ+LAoIAA7";
    private static final String INFO_GIF = "data:image/gif;base64,R0lGODlhDwAPAOYAAAAAAP////39/vn6/Pj5+9rg63KDntHZ5tXc6Nfe6dbd6AknU0ddfV15olZwlk9nikxjhUpggUVZeENXdElefkZaeWWBqmiErGeDq2qFrWyHrm2Ir3CKsFhsi3SNsnaPtGR5mHeQtHyUt36VuHeNrYCXuXWKqW6BnoSau4OZuoWbvHqOrIqfv3eJpIugv42hwJCkwoWXspmsx5irxoKSqZKiuoaVq6Gyy6a2zqm50K280qy70Z+swLG/1LC+07XC1rjF2LzI2sHM3cPO3sLN3bK8y8nT4sbQ38vU4s3W5MzV49Pb59La5sjP2ent8+Pn7fX3+vT2+fP1+EBUcEFVcXmOq32OppWpxZ6wyae0xbnG2MDJ1eDm7t/l7eHn7+Xq8eTp8PDz9+/y9t3k7dzj7Nvi6+fs8ubr8ezw9evv9Oru89jf5+Lo7/H09/b4+vv8/fr7/P7+/v///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAHIALAAAAAAPAA8AAAe7gHKCg4SFgmBmZ2BsXGQFCkxHg15xcQJvcANuUW1iai6HAgKZTR1PYmhOZ6ByXG8DBFA2C1tpZ4wlgmQEm21FNGtfXmVMHoIJUFJhaAYLWWMISEIYggfLamZWCzxKREA+DYJG2GBcLQs1Wj05Nw6CRGBdCkknCzHsMzAPgkBLSUNBQCyoIgMGixQRBOkA+GPHChMkWKAQ8UGCIBg/dODAcuWFihEhOGigMujChgwWHkCgUGHCFAaGYhIKBAA7";

    protected final NumberFormat numberFormat;
    protected final Locale locale;
    protected final ResourceBundle messages;
    protected final Charset encoding;
    protected final boolean includeToc;

    public AbstractSystemReportRenderer(final Sink sink, final ReportBuilder reportBuilder) {
        super(sink);
        locale = reportBuilder.locale;
        encoding = reportBuilder.encoding;
        messages = reportBuilder.resourcBundle;
        includeToc = reportBuilder.includeToc;
        Locale.setDefault(locale);
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(5);
    }

    public static String convertBytesToString(final Long bytes) {
        double b = bytes.doubleValue();

        for (int i = 0; i < 4; i++) {
            if (b < LIMIT) {
                return toString(b, i);
            }
            b = b / TEN_24;
        }
        return toString(b, 4);
    }

    private static String toString(final double b, final int i) {
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(3);
        return format.format(b) + ABBREVIATIONS[i];
    }

    public static String camelCaseToWords(final String name) {
        return WordUtils.capitalizeFully(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(name), " "));
    }

    public void renderNumericValue(final Double value, final String units) {
        renderNumericValue(value, units, null);
    }

    public void renderNumericValue(final Double value, final String units, final SinkEventAttributes attrs) {
        String text;
        if (value != null) {
            text = numberFormat.format(value.doubleValue());
            if (StringUtils.isNotBlank(units)) {
                text += " " + units;
            }
        } else {
            text = "--";
        }
        renderCellText(text, attrs);
    }

    public void renderCellText(final String text, final SinkEventAttributes attrs) {
        if (attrs != null) {
            sink.tableCell(attrs);
        } else {
            sink.tableCell();
        }
        sink.text(text);
        sink.tableCell_();
    }

    public void renderCellHeaderText(final String text) {
        renderCellHeaderText(text, null);
    }

    public void renderCellHeaderText(final String text, final SinkEventAttributes attrs) {
        if (attrs != null) {
            sink.tableHeaderCell(attrs);
        } else {
            sink.tableHeaderCell();
        }
        sink.text(getTranslatedText(text));
        sink.tableHeaderCell_();
    }

    public String getTranslatedText(final String messageCode, final Object... parameters) {
        try {
            return MessageFormat.format(messages.getString(messageCode), parameters);
        } catch (final Exception e) {
            return messageCode;
        }
    }

    public void renderExceptionText(final int colspan, final String exception) {
        final String exceptionId = RandomStringUtils.randomAlphabetic(10);
        sink.tableRow();
        renderCellText(" ");
        sink.tableCell(createColspan(colspan - 1));
        sink.unknown("a", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_START)},
                createAttrs(SinkEventAttributes.HREF, String.format("javascript:toggleException(\'err_%s')", exceptionId)));
        sink.text("Show/Hide Exception");
        sink.unknown("a", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_END)}, null);

        sink.unknown("pre", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_START)}, createAttrs(SinkEventAttributes.ID, "err_" + exceptionId));
        sink.text(exception);
        sink.unknown("pre", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_END)}, null);
        sink.tableCell_();
        sink.tableRow_();
    }

    public void renderCellText(final String text) {
        renderCellText(text, null);
    }

    public SinkEventAttributes createColspan(final int colspan) {
        return createAttrs(SinkEventAttributes.COLSPAN, Integer.valueOf(colspan));
    }

    public SinkEventAttributes createAttrs(final String attribute, final Object value) {
        final SinkEventAttributeSet set = new SinkEventAttributeSet();
        set.addAttribute(attribute, value);
        return set;
    }

    public void renderErrorIcon() {
        renderImage(ERROR_GIF);
    }

    public void renderImage(final String url) {
        sink.figure();
        sink.figureGraphics(url);
        sink.figure_();
    }

    public void renderSuccessIcon() {
        renderImage(SUCCESS_GIF);
    }

    public void renderInfoIcon() {
        renderImage(INFO_GIF);
    }

    public void renderPropertyValueRow(final String property, final String value) {
        sink.tableRow();

        renderCellHeaderText(getTranslatedText(property), createAttrs(SinkEventAttributes.STYLE, "width: 35%;"));
        renderCellText(value);

        sink.tableRow_();
    }

    public void renderPropertyValueRow(final String property, final int colspan, final String value) {
        sink.tableRow();

        renderCellHeaderText(property, createAttrs(SinkEventAttributes.STYLE, "width: 35%;"));
        renderCellText(value, createColspan(colspan));

        sink.tableRow_();
    }

    public void renderPropertyValueRow(final String property, final String value1, final String value2) {
        sink.tableRow();

        renderCellHeaderText(property, createAttrs(SinkEventAttributes.STYLE, "width: 35%;"));
        renderCellText(value1);
        renderCellText(value2);

        sink.tableRow_();
    }

    public String formatInstant(final Date instant) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(instant);
    }

    public String formatInstantTime(final Date instant) {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(instant);
    }

    @Override
    public void render() {
        sink.head();

        renderCustomHeaderContent();
        sink.title();
        text(getTitle());
        sink.title_();

        sink.head_();

        sink.body();
        renderTableOfContentsStart();
        renderBody();
        renderEndOfReportContent();
        sink.body_();

        sink.flush();

        sink.close();
    }

    @Override
    public String getTitle() {
        return getTranslatedText("report.title");
    }

    public void renderCustomHeaderContent() {
        try {
            final String css = IOUtils.toString(getClass().getResourceAsStream("/report-style.css"), encoding);
            final String printCss = IOUtils.toString(getClass().getResourceAsStream("/print.css"), encoding);

            if (sink instanceof CustomSiteSink) {
                ((CustomSiteSink) sink).style(css, createAttributeBuilder()
                        .addAttribute(SinkEventAttributes.TYPE, "text/css")
                        .addAttribute("media", "all").build());

                ((CustomSiteSink) sink).style(printCss, createAttributeBuilder()
                        .addAttribute(SinkEventAttributes.TYPE, "text/css")
                        .addAttribute("media", "print").build());
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void renderTableOfContentsStart() {
        sink.section1();
        sink.unknown("div", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_START)}, createAttrs(SinkEventAttributes.CLASS, "hideFromPrint"));
        sink.sectionTitle1();
        sink.text(getTranslatedText("table.of.contents"));
        sink.sectionTitle1_();
        sink.unknown("div", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_END)}, null);
        sink.unknown("div", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_START)}, createAttrs(SinkEventAttributes.ID, "tableofcontents"));
        sink.unknown("div", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_END)}, null);
        sink.section1_();
    }

    protected void renderEndOfReportContent() {
        renderTableOfContentsEnd();
    }

    public static AttributeBuilder createAttributeBuilder() {
        return new AttributeBuilder();
    }

    private void renderTableOfContentsEnd() {
        final String script = "\nvar loadTag = function(element, tag) {" +
                "var i = 0, subElements = element.getElementsByTagName(tag);" +
                "for (; i < subElements.length; i++) {" +
                "if (subElements[i]) {" +
                "return subElements[i];" +
                "} } return null; };" +
                "var loadLevel= function(element, level) {" +
                "var header, a, text, listText = '', i = 0, headers = element.getElementsByTagName(\"h\" + level);" +
                "if (headers.length > 0) {" +
                "listText += '<ol>';" +
                "for (; i < headers.length; i++) {" +
                "header = headers[i];" +
                "a = loadTag(header, 'a');" +
                "text = header.innerText || header.textContent;" +
                "listText += '<li><a href=\"#' + a.getAttribute('name') + '\">' + text + '</a>';" +
                "if (level < 5) {" +
                "listText += loadLevel(header.parentElement, level + 1);" +
                "} listText += '</li>'; } listText += '</ol>'; } return listText; };" +
                "(function() {var toc = document.getElementById('tableofcontents');" +
                "toc.innerHTML = loadLevel(document, 2);})();\n //";
        sink.unknown("script", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_START)}, createAttrs(SinkEventAttributes.TYPE, "text/javascript"));

        sink.unknown("cdata", new Object[]{Integer.valueOf(HtmlMarkup.CDATA_TYPE), script}, null);

        sink.unknown("script", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_END)}, null);
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    protected void renderPageBreak() {
        sink.unknown("div", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_START)}, createAttrs(SinkEventAttributes.CLASS, "pageBreak"));

        sink.unknown("div", new Object[]{Integer.valueOf(HtmlMarkup.TAG_TYPE_END)}, null);
    }

    public static class AttributeBuilder {
        private final Map<String, Object> attrs = new HashMap<>();

        public AttributeBuilder addAttribute(final String attribute, final Object value) {
            attrs.put(attribute, value);
            return this;
        }

        public SinkEventAttributeSet build() {
            final SinkEventAttributeSet attrSet = new SinkEventAttributeSet();
            attrs.entrySet().forEach(entry -> {
                attrSet.addAttribute(entry.getKey(), entry.getValue());
            });
            return attrSet;
        }
    }

}